### UIDs comparison
UUID, ULID, Snowflake ID, and KSUID are all different types of unique identifiers commonly used in distributed systems. Below is an explanation of each and the key differences among them:

1. UUID (Universally Unique Identifier)
   Format: Typically 128 bits (commonly represented as a 36-character string, with hyphens, e.g., 123e4567-e89b-12d3-a456-426614174000).
   Structure: UUIDs are divided into different versions, the most common being v1 (based on timestamp and MAC address), v4 (randomly generated), and v5 (based on names using SHA-1).
   Characteristics:
   Globally unique.
   Version 4 UUIDs are purely random, while v1 relies on the current timestamp and node (like MAC address).
   Pros: Simple to generate; globally unique; no central coordination is needed.
   Cons: Lack of ordering; UUID v4 is randomly generated, so it can result in fragmented database indexes, which can impact performance.
2. ULID (Universally Unique Lexicographically Sortable Identifier)
   Format: 128-bit identifier, represented as a 26-character string (01ARZ3NDEKTSV4RRFFQ69G5FAV).
   Structure: Combines 48 bits of timestamp with 80 bits of randomness.
   Characteristics:
   Designed to be lexicographically sortable, meaning IDs generated closer in time will be closer in their sorted order.
   Useful for ordered data in distributed systems.
   Pros: Keeps partial ordering based on time, making it suitable for distributed logging and event-sourcing where ordering is important.
   Cons: Randomness component still leads to a slight risk of collision in distributed environments, but it's negligible.
3. Snowflake ID (Twitter Snowflake)
   Format: 64-bit integer, often represented as a number (1013622638998458368).
   Structure:
   Composed of timestamp bits, datacenter/machine identifier bits, and sequence number bits.
   Typically, the structure involves 41 bits for timestamp, 10 bits for datacenter/machine identifier, and 12 bits for sequence number, allowing 4096 unique IDs per millisecond per machine.
   Characteristics:
   Time-ordered IDs that allow easy sharding and sorting.
   No central coordination is required, as each machine or datacenter has a unique identifier to help ensure uniqueness.
   Pros: Compact; ordered by time; IDs are shorter than UUIDs, which is efficient for storage and indexing.
   Cons: Typically requires specific infrastructure or a central server to coordinate the distribution of machine IDs.
4. KSUID (K-Sortable Unique Identifier)
   Format: 160-bit identifier, represented as a 27-character string (0ujsszd6t4weR0M41A1E8aXiM3f).
   Structure:
   Uses 32 bits for timestamp (seconds since the UNIX epoch) and 128 bits for randomness.
   Characteristics:
   Like ULID, it is lexicographically sortable, meaning that the generated IDs are ordered by creation time.
   Longer (160 bits) than ULID.
   Pros: Highly suitable for cases where order is important, such as logs or distributed databases. It offers global uniqueness without a central authority.
   Cons: Longer compared to UUID and Snowflake, which can make it slightly less efficient in terms of storage.
   Summary of Differences
   Identifier	Bits	Time-based Component	Sortable	Randomness	Usage Characteristics
   UUID	128	Optional (v1)	No	Yes (v4)	Simple, widely used, not sortable
   ULID	128	48 bits	Yes	80 bits	Sortable, human-readable, distributed systems
   Snowflake	64	41 bits	Yes	Sequence + Machine bits	Compact, ordered IDs, easy sharding
   KSUID	160	32 bits	Yes	128 bits	Sortable, human-readable, large randomness, suited for distributed systems
   Which One to Use?
   UUID is great for globally unique IDs when ordering is not important.
   ULID is a good choice for systems where lexicographical order is useful, such as logs or event stores.
   Snowflake ID is well-suited for applications requiring high-frequency ID generation with time ordering and sharding, such as social media posts or distributed services.
   KSUID offers benefits similar to ULID, but with larger randomness and is a good fit for distributed systems where you want natural ordering and uniqueness without coordination.
   Each identifier has its own use cases and is designed to address different aspects of distributed systems, from uniqueness to efficiency and ordering requirements.