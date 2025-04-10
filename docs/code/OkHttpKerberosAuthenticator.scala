import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import org.ietf.jgss.*;
import java.util.Base64;

public class KerberosAuthenticator implements Authenticator {
    private final String servicePrincipal;

    public KerberosAuthenticator(String servicePrincipal) {
        this.servicePrincipal = servicePrincipal;
    }

    @Override
    public Request authenticate(Route route, Response response) {
        try {
            // Log in with Kerberos (JAAS must be configured)
            LoginContext lc = new LoginContext("KrbLogin");
            lc.login();
            Subject subject = lc.getSubject();

            // Generate Kerberos token using the GSS-API:
            String token = Subject.doAs(subject, (java.security.PrivilegedExceptionAction<String>) () -> {
                GSSManager manager = GSSManager.getInstance();
                // Kerberos OID for Kerberos V5
                Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
                GSSName serverName = manager.createName(servicePrincipal, GSSName.NT_HOSTBASED_SERVICE);
                GSSContext context = manager.createContext(serverName, krb5Oid, null, GSSContext.DEFAULT_LIFETIME);
                context.requestMutualAuth(true);
                byte[] tokenBytes = context.initSecContext(new byte[0], 0, 0);
                return Base64.getEncoder().encodeToString(tokenBytes);
            });

            // Rebuild the request with the Authorization header:
            return response.request().newBuilder()
                    .header("Authorization", "Negotiate " + token)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            // Return null if authentication fails so that no further attempts are made
            return null;
        }
    }

    // Example of creating an OkHttpClient with the authenticator:
    public static OkHttpClient createClient(String servicePrincipal) {
        return new OkHttpClient.Builder()
                .authenticator(new KerberosAuthenticator(servicePrincipal))
                .build();
    }
}
