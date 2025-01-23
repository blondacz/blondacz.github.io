
**Actor-Based** **Event-Sourcing** System with **CQRS**, **Transactionality**, and **High Availability**

- Fits into overall architecture of **event-driven microservices** connected by **brokers** 
- Each **domain entity** represented as an **actor** and **commands** are routed to the specific actor.
- **High throughput** processing with **transactional** depth-first traversal (around 15K BTS)
- _All-or-Nothing Event Persistence_ 
- Together with developed **watermarks recovery** patterns guarantees exactly one processing
- **Replication Before Persistence** and **Publication After Replication**
- **Storage in RocksDB**
- **Decoupled Read and Write Models** 
-  **Primary-Secondary Setup**  