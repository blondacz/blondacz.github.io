# Consul CLI Cheat-Sheet for KV-based Application Leadership

> Assumes your leadership lock key is something like `app/leadership/lock` and is held by a **Consul session**. Replace paths/names as needed.

---

## Prereqs (env vars & context)

Set these once so you don’t have to repeat flags:

```bash
export CONSUL_HTTP_ADDR="http://<consul-host>:8500"
export CONSUL_HTTP_TOKEN="<acl-token>"   # if ACLs are enabled
# export CONSUL_CACERT=/path/to/ca.pem   # if TLS
# export CONSUL_CLIENT_CERT=/path/to/cert.pem
# export CONSUL_CLIENT_KEY=/path/to/key.pem
```

Define your lock key for the snippets below:

```bash
LOCK_KEY="app/leadership/lock"
```

---

## 1) Cluster / Agent / Raft state

```bash
# Members (servers & clients) and their status
consul members -detailed

# Agent & server runtime info (serf, raft, build, KV, health, etc.)
consul info

# Server Raft peers (shows the Consul cluster leader and voters)
consul operator raft list-peers
```

---

## 2) Services & Nodes

```bash
# All known service names
consul catalog services

# Service names with tags
consul catalog services -tags

# Nodes running a specific service
consul catalog nodes -service <service-name>

# All nodes (optionally detailed)
consul catalog nodes
consul catalog nodes -detailed

# Services registered on the *local* agent only (quick sanity check)
consul services
```

---

## 3) Inspect the KV Lock & Session

```bash
# Get the lock value (often contains node/service identity or payload)
consul kv get "$LOCK_KEY"

# Include metadata – crucial to see the holding Session ID
consul kv get -detailed "$LOCK_KEY"

# If you only want the Session UUID (one-liner)
consul kv get -detailed "$LOCK_KEY" | awk '/^Session/ {print $2}'
```

_If a session currently holds the lock, you’ll see a line like:_
```
Session: 8a4b1b3d-...-c0b1
```

Optional deep-dive on sessions:

```bash
# List sessions known to the local agent
consul session list

# Inspect a specific session
consul session info <session-id>
```

---

## 4) Trigger a New Leadership Election

There are two common, safe ways:

### A) Destroy the holding Session (preferred)
Releases the lock immediately. If the lock key/session was created with `behavior=delete`, the key will be removed; otherwise it’s just released.

```bash
# 1) Extract the current session ID that holds the lock:
SID=$(consul kv get -detailed "$LOCK_KEY" | awk '/^Session/ {print $2}')

# 2) If there is a session, destroy it:
[ -n "$SID" ] && consul session destroy "$SID"
```

### B) Delete the Lock Key (forces contenders to retry)
```bash
consul kv delete "$LOCK_KEY"

# Or delete a whole subtree (use with care)
consul kv delete -recurse app/leadership/
```

> **Notes**
> - `behavior=release` (default): session destruction **releases** the key (key remains, lock freed).
> - `behavior=delete`: session destruction **deletes** the key (ephemeral entry semantics).

---

## 5) Watch the Lock Change (handy for debugging)

```bash
consul watch -type=key -key="$LOCK_KEY"   'echo "$(date) — lock update"; consul kv get -detailed '"$LOCK_KEY"'; echo "-----"'
```

This prints details any time the lock key changes (acquired, released, value updates, etc.).

---

## 6) Quick Triage Recipes

### Current leader (value + session) in one go
```bash
echo "Key: $LOCK_KEY"
consul kv get "$LOCK_KEY"
echo "---"
consul kv get -detailed "$LOCK_KEY" | awk '/^Session/ {print "Session:", $2}'
```

### Force re-election in one line (session-aware)
```bash
SID=$(consul kv get -detailed "$LOCK_KEY" | awk '/^Session/ {print $2}'); if [ -n "$SID" ]; then consul session destroy "$SID"; else consul kv delete "$LOCK_KEY"; fi
```

### See who would be next (by watching contenders’ heartbeats)
If your contenders publish heartbeat/identity under a prefix (example: `app/leadership/candidates/`):

```bash
consul kv export app/leadership/candidates/ | sed -n '1,200p'
# or just list keys/values
consul kv get -recurse app/leadership/candidates/
```

---

## 7) Common Gotchas

- **ACLs:** Most commands above need read or write on `kv`, `session`, `catalog`, and `operator` paths. If you see permission errors, check your token’s policy.
- **Where you run the command:** `consul services` queries the **local agent**; `consul catalog ...` queries the **cluster catalog** (use the latter to see the global view).
- **Stale reads:** By default, reads are strongly consistent where applicable; if you’re debugging from WAN/laggy links, prefer running commands on a server or a local agent configured to talk to the right datacenter.

---

## 8) Minimal Runbook (copy/paste)

```bash
# Setup
export CONSUL_HTTP_ADDR="http://<consul-host>:8500"
export CONSUL_HTTP_TOKEN="<acl-token>"
LOCK_KEY="app/leadership/lock"

# Check cluster
consul members -detailed
consul operator raft list-peers

# Inspect leadership
consul kv get "$LOCK_KEY"
consul kv get -detailed "$LOCK_KEY"

# Trigger re-election (session-aware)
SID=$(consul kv get -detailed "$LOCK_KEY" | awk '/^Session/ {print $2}'); if [ -n "$SID" ]; then consul session destroy "$SID"; else consul kv delete "$LOCK_KEY"; fi

# Watch for changes while testing
consul watch -type=key -key="$LOCK_KEY" 'date; consul kv get -detailed '"$LOCK_KEY"
```
