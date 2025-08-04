## EPIC: Platform Onboarding (13 d)

1. **Team ramp-up: OpenShift fundamentals workshop** (3 d)  
   A hands-on lab covering OpenShift architecture, CLI (`oc`), project/namespace setup, and basic troubleshooting (logs, exec, resource quotas). Goal: everyone can deploy and debug apps in our corporate cluster.

2. **Team ramp-up: Redis basics and client libraries** (2 d)  
   Interactive deep-dive into Redis data types, persistence modes (RDB/AOF), eviction policies, and simple benchmarks. Walk through sample Scala clients (e.g. Redisson or Lettuce) and best practices for connection pooling.

3. **Team ramp-up: Apache Flink fundamentals** (3 d)  
   Instructor-led sessions on Flink’s streaming runtime, event vs. processing time, stateful operators, checkpointing, and fault tolerance. Build and submit a “WordCount” job locally to cement concepts.

4. **Team ramp-up: Kubernetes core concepts** (3 d)  
   Training on pods, Deployments, StatefulSets, Services, ConfigMaps, Secrets, resource requests/limits, and common `kubectl` troubleshooting patterns. Also cover rolling updates, rollbacks, and quota management.

5. **Team ramp-up: Scala & Gradle build basics** (2 d)  
   Workshop on Scala syntax and ecosystem, Gradle project structure, multi-module builds, dependency management, and writing unit tests with ScalaTest. Build and run sample pipelines to validate our Gradle setup.

---

## EPIC: Infrastructure Provision (6 d)

1. **Provision OpenShift project/namespace** (1 d)  
   Create our dedicated project, set resource quotas, default limits, assign team roles, and verify access.

2. **Configure network policies & service accounts** (1 d)  
   Define `NetworkPolicy` objects to lock down pod-to-pod traffic, create ServiceAccounts for each component, and apply least-privilege RBAC roles.

3. **Deploy Redis cluster (StatefulSet + config)** (2 d)  
   Author StatefulSet manifest for Redis (Sentinel or cluster mode), PVCs for persistence, readiness/liveness probes, TLS if required, and ConfigMaps for tunables.

4. **Deploy Flink cluster (JobManager & TaskManager)** (2 d)  
   Write Kubernetes manifests (or Helm chart) to bring up Flink JobManager and TaskManagers, configure heap sizes and checkpoint storage, and expose the Flink Web UI via an OpenShift Route.

---

## EPIC: Data Ingestion Pipeline (9 d)

1. **Define Flink job to consume SOW Topic from 60East AMPS** (2 d)  
   Configure and test Flink’s connector (Kafka/JMS) to subscribe to the SOW Topic, validate deserialization, and measure baseline throughput.

2. **Implement identifier extraction & minimal payload logic** (3 d)  
   In the Flink job, parse messages to extract CUSIP/PME IDs plus minimal metadata (timestamp, last-updated). Discard all other fields to minimize cache footprint.

3. **Serialize & write to Redis (key design)** (2 d)  
   Design a Redis key schema (`instr:{CUSIP}` → JSON blob), implement the sink in Scala/Gradle, add TTL logic if needed, and ensure idempotent writes for replayed events.

4. **Handle schema evolution & bad data in Flink** (2 d)  
   Add a side-output stream for malformed messages to a dead-letter Kafka topic or S3 bucket, and implement version-tolerant Avro/JSON deserialization to handle future schema changes.

---

## EPIC: Cache Lookup Service (5 d)

1. **Design service interface (REST/gRPC) for lookups** (1 d)  
   Define OpenAPI spec or gRPC `.proto` file for endpoints that accept CUSIP/PME IDs and return the minimal lookup payload, including error codes and validation rules.

2. **Implement service to transform CUSIP/PME → Redis key** (2 d)  
   Build the Scala service logic that constructs Redis keys, executes lookups, handles cache misses (e.g. return 404 or fallback), and records metrics (latency, error rates).

3. **Integrate caching library & connection pooling** (1 d)  
   Use a Scala Redis client (e.g. Lettuce) with pooled connections, configure timeouts/retries, and instrument hit/miss ratios via Micrometer or Prometheus.

4. **Deployment manifest (Deployment, Service, Route)** (1 d)  
   Create Kubernetes Deployment, Service, and OpenShift Route definitions, configuring TLS termination, health probes, and resource limits.

---

## EPIC: CI/CD Pipelines (10 d)

1. **Ramp-up: Custom Jenkins-layer training** (2 d)  
   Review our organization’s existing Jenkins “wrapper” layer: examine shared libraries, scripted pipelines, and conventions. Hands-on clinic to clone, run, and step through a sample pipeline.

2. **Refactor & document existing Jenkins wrapper** (2 d)  
   Clean up poorly structured pipeline code, remove dead steps, modularize common functions, and write internal docs so the team can easily author new pipelines.

3. **Implement Gradle build pipelines for Scala projects** (1 d)  
   Create reusable Jenkins pipeline stage definitions that compile Scala code with Gradle, run tests, and publish artifacts to Nexus/Maven.

4. **Build Flink job CI pipeline** (1 d)  
   Define a Jenkinsfile that checks out the flink-job repo, runs `gradle build`, executes unit tests, and archives the JAR.

5. **Build Lookup Service CI pipeline** (1 d)  
   Create a Jenkinsfile for the Scala lookup service: `gradle build`, run integration tests (mocking Redis), build and push Docker image to our registry.

6. **Helm/OC templates for infra & apps** (2 d)  
   Develop parameterized Helm charts or `oc process` templates for Redis, Flink, and the lookup service, covering dev/stage/prod and enabling quick rollbacks.

7. **Automated deployment jobs (dev → prod)** (2 d)  
   Configure Jenkins to trigger deployments on successful CI builds, deploy first to dev (smoke tests), then promote to staging/production with manual gates.

---

## EPIC: Security & Compliance (5 d)

1. **Secrets management (Vault/Openshift secrets)** (1 d)  
   Integrate HashiCorp Vault or leveraging OpenShift Secrets for Redis credentials, Flink checkpointing tokens, and TLS certs; ensure pipelines retrieve secrets at deploy time.

2. **Network policy / firewall rules** (1 d)  
   Enforce strict pod-to-pod traffic via Kubernetes `NetworkPolicy`, coordinate cluster firewall rules, and validate no unauthorized access paths.

3. **RBAC roles & SCA for container images** (1 d)  
   Create ServiceAccount role bindings for each namespace, and add a security-scan stage (Trivy/Clair) to CI pipelines to catch vulnerabilities.

4. **Penetration test plan & addressing findings** (2 d)  
   Draft a pen-test scope for REST endpoints, Redis auth, and Flink UI; execute tests (in-house or via 3rd party) and triage any findings.

---

## EPIC: High-Availability & DR (7 d)

1. **Redis HA setup (Sentinel or cluster mode)** (2 d)  
   Configure Redis with Sentinel for master/slave failover (or native cluster) and test pod-kill scenarios to validate automatic failover.

2. **Flink job restart & checkpointing strategy** (1 d)  
   Enable exactly-once checkpoints to durable storage (e.g. S3/PVC), configure restart strategies, and run failure drills to verify state recovery.

3. **Disaster recovery runbook & drills** (2 d)  
   Document step-by-step DR procedures: restore Redis from backups, redeploy Flink and lookup service, and validate end-to-end functionality in a secondary region/project.

4. **Cross-region backup for Redis persistence** (2 d)  
   Automate RDB/AOF snapshot exports to a remote blob store and script restore steps, including any necessary network reconfiguration.

---

## EPIC: Testing & Validation (7 d)

1. **Unit tests for Flink transformations** (1 d)  
   Write ScalaTest or JUnit specs for each Flink operator to verify identifier extraction, error-handling, and payload formatting.

2. **Integration tests (end-to-end data flow)** (2 d)  
   Spin up Flink, Redis, and the lookup service in an ephemeral OpenShift namespace (or testcontainers), publish a sample message, and assert correct cache writes and lookup responses.

3. **Load/performance test for cache lookups** (2 d)  
   Use Gatling or JMeter to simulate our target SLA (e.g. 1,000 req/s), measure 95th-percentile latency, and tune thread pools/Redis configs.

4. **Chaos-testing infra resilience** (2 d)  
   Introduce pod/network failures during an active Flink job to verify checkpointing and service continuity; record any gaps.

---

## EPIC: Documentation & Handover (4 d)

1. **README, runbooks, on-call playbooks** (2 d)  
   Create clear docs on build & deployment steps, operational runbooks for common incidents, Slack alerting config, and PagerDuty run-books.

2. **Knowledge-transfer sessions & recordings** (2 d)  
   Demo the end-to-end pipeline, record sessions, capture Q&A, and share slide decks so other teams can maintain and extend.
