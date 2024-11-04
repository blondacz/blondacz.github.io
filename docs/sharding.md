# Sharding using ULIDs notes

> __**Note:**__ the approach bellow works when data/transactiosn have same priority/value and does not take risk into account.

### A. Single id (ULID) based sharding
It is possible to combine **time-based** **and hash-based** sharding:

The timestamp portion of the ULID  can be used to roughly partition data into time buckets by day or week or month.
Within each time bucket a hash of the full ULID can be used to distribute the data evenly across multiple sub-shards.
Logical Shards for Hot Spot Mitigation:

This will also eliminate hot spots compared to just time based sharding.
It is also possible to use hash based sharding as primary key and the time portion as secondary index for range queries.  


### B. Attribute-Aware Sharding Approach
If it is required to incorporate **transaction-specific attributes** like the **type of market** or the presence of a **high-value client** when determining how to shard your data, a more **attribute-aware sharding** strategy is appropriate. 
This ensures that related transactions or transactions with specific attributes are grouped or separated based on your requirements.

### **Approaches for Attribute-Aware Sharding**

#### 1. **Composite Sharding Key**
A **composite sharding key** can be formed by combining a **unique identifier** like ULID/KSUID with an **attribute** such as the **market type** or **client tier**.

**How It Works**:
- **Attribute-Based Prefix**: An attribute value as a prefix for the sharding key. For instance:
    - If the **market type** is an attribute, ULID is prefixed with `market_type_id` to create a key like `1-01ARZ3NDEKTSV4RRFFQ69G5FAV`, where `1` represents "market type 1."
    - This prefix can be used to **determine the shard** based on the market type, while the ULID ensures uniqueness and order.

**Example Sharding Strategy**:
- It is then possible to **hash** the composite key to determine the shard, ensuring that:
    - Transactions related to a specific **market type** or **client category** are grouped together.
    - High-value transactions are automatically routed to **dedicated shards** for better security or isolation.

#### 2. **Custom Hash Function Based on Attributes**
A **custom hash function** can incorporates specific **attributes of a transaction** (such as client ID, market type, or transaction risk score) into the **sharding logic**.

**How It Works**:
- Instead of hashing only the **unique identifier** (e.g., ULID),  the **market type** or **client type** ais included as an input to the hash function.
- The final **shard ID** is determined by the output of the hash function:
  ```scala
  val shardId = hash(attribute_value + unique_id) % number_of_shards
  ```

**Benefits**:
- Number of shard can be actually double layered so that virtual shards map to physical shards, such approach give more flexibility/independence in shard mapping to actual hosts
- This ensures that transactions for **different markets** or **client tiers** are **evenly distributed** across shards, with **attribute-based segregation**.
- It is also possible to control which **high-value clients** go to specific shards by tweaking the hash function or providing explicit shard mapping for special cases.

#### 3. **Lookup Table for Attribute-Based Sharding**
A **shard lookup table** that maps certain attributes to specific shards can be used. Lookup table can be managed in distributed cache.

**Benefits**:
- This approach provides **fine-grained control** over how specific attributes influence the sharding decision.
- It is easy to **rebalance** data or isolate certain high-risk/high-value segments by updating the lookup table.

**Use Case**:
- If you have **high-value clients** whose data you want to keep isolated for better performance, the lookup table can ensure they are assigned to a specific shard dedicated to such transactions.

#### 4. **Range-Based and Attribute-Aware Sharding Combination**
Combining **range-based sharding** with attribute-aware logic can also be effective when dealing with attributes that influence shard assignment.

**How It Works**:
- First, determine which **attribute-based group** a transaction belongs to (e.g., high-value vs. low-value clients).
- Then use a **range-based sharding** strategy for each group:
    - For instance, all **high-value clients** are mapped to shards 1-5, while all **low-value clients** are mapped to shards 6-10.
    - Within these shard groups, use a **range-based** or **hash-based** strategy to distribute data more granularly.

**Example**:
- For a **financial application**, you could separate transactions for different **market types** using attribute-based sharding, and then use **range-based sharding** to further split high-volume markets into multiple shards.

#### 5. **Attribute Tagging and Secondary Shards**
In cases where transactions have **multiple important attributes** (e.g., client type, market type, risk level), you can adopt a **secondary shard tagging** approach.

**How It Works**:
- Assign each transaction a **primary shard** based on a main attribute, such as **client type**.
- Optionally, assign the transaction a **secondary shard tag** based on another attribute, like **market type**.
- Use a **multi-dimensional routing strategy** to ensure that the transactions can be routed to the appropriate shard based on queries related to either attribute:
    - For **writes**, use the **primary attribute**.
    - For **reads**, allow for efficient lookup based on both **primary** and **secondary** attributes using a **shard mapping** mechanism.

**Example**:
- If a high-value client in **market A** performs a transaction, the primary shard is determined by the **client type**, and the secondary shard can help segregate market-specific information.


### **Considerations for Attribute-Based Sharding**
1. **Data Skew**:
    - Ensure that the attribute values used in the sharding key do not lead to **data skew**. For instance, if most transactions belong to a specific market, those shards might become **overloaded**.
    - To mitigate skew, consider using **hybrid sharding** where attributes are combined with **random or hash-based** distribution to maintain balance.

2. **Rebalancing**:
    - When adding or removing shards, **rebalancing** data is more complex when using attribute-based sharding compared to simpler hash-based methods. You may need to update the lookup table or composite key mappings.
    - Tools like **consistent hashing** can help minimize data movement during shard reconfiguration.

3. **High-Value Clients**:
    - If high-value clients need to be isolated, consider **dedicated shards** for these clients. This isolation can improve **performance** and **security** but requires additional resources.
