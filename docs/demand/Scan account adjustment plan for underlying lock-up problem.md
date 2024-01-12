### 1. Overall plan.
1. The special node is responsible for collecting and saving abnormal data of underlying adjustments, and providing a query interface to the agent. The data is commission and pledge deduction details.
2. When the upgrade proposal takes effect, the agent queries the special node exception data interface and adjusts delegation, pledge, and statistical data.

### 2. Exception process
> Because the data complexity of the live network is much higher than that of the test, if the agent encounters data anomalies during the account adjustment process, manual intervention is required.

### 3. Detailed design

#### 3.1 Special node account adjustment interface

```
Input parameters: blockNumber -- block height
Output parameters: [
{
"optType":"", -- operation type staking, delegate
"nodeId":"", -- node id
"stakingBlockNum":"", -- Validator block height
"addr":"", -- pledge or delegation address
"lock":"", -- the lock amount to be reduced
"has":"" -- The hesitation amount to be reduced
}
]

```

#### 3.2 Special node design

1. Add a new database and save the adjustment information with
2. After the execution of the underlying compatibility logic is completed, the adjustment information will be stored in the database
3. Provide contract query interface

#### 3.3 agent design

##### 3.3.1 Execution timing
> Executed high in the upgrade proposal ActiveBlock block, before the transaction is executed. And there is data in the adjustment interface.

##### 3.3.2 Commission information adjustment

##### 3.3.2.1 Adjustment preparation

- List<DiffItem> adjustParamList: Commissioned adjustment details
- List<Delegation> delegationList: List of delegation information to be adjusted
- List<Staking> stakingList: pledge information associated with the delegation
- List<Node> nodeList: node information associated with the delegate
- List<Address> addressList: address information, the current mechanism has been loaded into the process memory

##### 3.3.2.2 Account adjustment logic

1. Amount verification. If it fails, the detailed adjustment will fail. delegation.delegate_hes >= diffItem.has and delegation.delegate_locked >= diffItem.locked
2. Commission information adjustment (same as commission redemption logic)

##### 3.3.2.3 Account adjustment results
> The results are output to the specified log. diff.log

```
blockNumber=number | srcData=JSON.string(diffItem) | result = (success|error)

```

##### 3.3.3 Pledge information adjustment

##### 3.3.3.1 Adjustment preparation

- List<DiffItem> adjustParamList: Pledge details to be adjusted
- List<Staking> stakingList: Staking information to be adjusted
- List<Node> nodeList: node information associated with the delegate
- List<Address> addressList: address information, the current mechanism has been loaded into the process memory

##### 3.3.3.2 Account adjustment logic

1. Amount verification. If it fails, the detailed adjustment will fail. staking.staking_hes >= diffItem.has and staking.staking_locked >= diffItem.locked
2. Adjustment of pledge information (amount deduction follows the same logic as entrustment release of pledge, if the deduction is less than the minimum pledge threshold, the pledge will be withdrawn)

##### 3.3.3.3 Account adjustment results
> The results are output to the specified log. diff.log

```
blockNumber=number | srcData=JSON.string(diffItem) | result = (success|error)

```

##### 3.3.4 Account adjustment completed.

- No error: Execute normally after exiting the adjustment logic.
- With errors: Exit after printing error log. (reconciliation failed)


#### 3.4 Detailed compensation for failure
1. Based on the data spit out by the agent, manually analyze the reasons and generate a sql script.
2. Execute the sql script.
3. Start the agent in non-compensation mode.

### 4. Impact on users
1. If the entrustment relationship is canceled during the account adjustment process and there are rewards in the entrustment, aton and scan will not be able to see the reward collection record.
2. If the entrustment relationship is canceled during the account adjustment process, my entrustment record will disappear and there will be no transaction record.