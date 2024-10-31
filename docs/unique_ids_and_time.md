### Unique IDs and time
### Comparison of UUID, ULID, Snowflake ID, and KSUID

#### UUID (Universally Unique Identifier)
- **Format**: 128 bits, typically represented as a 36-character string with hyphens (e.g., `123e4567-e89b-12d3-a456-426614174000`).
- **Structure**:
    - **v1**: Based on timestamp and MAC address.
    - **v4**: Randomly generated.
    - **v5**: Name-based using SHA-1.
- **Characteristics**:
    - **Globally unique**, easy to generate.
    - **v4** relies on randomness, leading to fragmentation in databases.
- **Use Cases**: General purpose ID generation for distributed systems.

#### ULID (Universally Unique Lexicographically Sortable Identifier)
- **Format**: 128-bit identifier represented as a 26-character string.
- **Structure**:
    - **48 bits** for timestamp (in milliseconds).
    - **80 bits** for randomness.
- **Characteristics**:
    - **Lexicographically sortable** for IDs generated close in time.
    - Allows for **natural ordering**.
- **Use Cases**: Distributed logging, event sourcing, scenarios requiring easy ordering.

#### Snowflake ID (Twitter Snowflake)
- **Format**: 64-bit integer represented as a number.
- **Structure**:
    - **41 bits** for timestamp.
    - **10 bits** for datacenter/machine.
    - **12 bits** for sequence number.
- **Characteristics**:
    - **Time-ordered** IDs, compact for storage.
    - Requires specific infrastructure for unique machine IDs.
- **Use Cases**: High-write rate environments, easy database sharding.

#### KSUID (K-Sortable Unique Identifier)
- **Format**: 160-bit identifier represented as a 27-character string.
- **Structure**:
    - **32 bits** for timestamp (seconds since epoch).
    - **128 bits** for randomness.
- **Characteristics**:
    - **Lexicographically sortable**, but larger than ULID.
    - Suitable for **high-scale** distributed environments.
- **Use Cases**: Similar to ULID, but more robust to collision due to higher randomness.

### Monotonically Increasing ULID vs KSUID

#### What is a Monotonically Increasing ULID?
- **Monotonically Increasing ULID** ensures IDs are strictly incremented, even if generated within the same millisecond.
- Used to **maintain order** when generating multiple IDs very quickly.
- Requires **state tracking** of the last generated ULID to increment within the same millisecond.

#### How KSUID and Monotonically Increasing ULID Handle Time Drift
- **Time Drift Issue**: If the system clock moves backward due to **NTP synchronization**, there can be ordering violations.

**KSUID**:
- Uses **seconds-level precision**; smaller adjustments are less impactful.
- Generates **random IDs** with collision resistance from **128 bits of randomness**.
- Mitigation may include local counters or waiting for the clock to correct itself.

**Monotonically Increasing ULID**:
- **Sensitive** to backward time jumps, as it maintains strict ordering.
- Uses an approach like **artificial time advancement** to prevent timestamp regression.
- State tracking is critical, and monotonic increment ensures order within the same millisecond.

### Monotonically Increasing KSUID?
- There is **no native monotonically increasing KSUID**.
- KSUID is **stateless** and optimized for **global uniqueness** in a distributed system, making a monotonic version counter to its purpose.
- Potential Alternatives:
    - Adding **statefulness** by maintaining a counter.
    - Hybrid approach to use randomness with an incremented component.
    - Logical clocks or application-level monotonic logic.

### Summary
- **KSUID** is designed to be **stateless** and **globally unique** without needing coordination, which means that making it **monotonically increasing** adds complexity.
- **Monotonically Increasing ULID** is ideal for use cases requiring **strict ordering**, but it requires **state management**, making it less suitable for fully distributed environments.

If **strict order** is needed, **Monotonically Increasing ULID** may be a good fit; if **stateless, distributed generation** is more important, **KSUID** is preferable, accepting some trade-offs regarding ordering.

