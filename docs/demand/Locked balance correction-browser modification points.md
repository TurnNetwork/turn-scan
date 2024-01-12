# Prerequisite: Query the interface provided by the special node and obtain the following account adjustment data structure:
[
{
"optType":"", -- operation type staking, delegate
"nodeId":"", -- node id
"stakingBlockNum":"", -- Validator block height
"addr":"", -- pledge or delegation address
"lock":"", -- the lock amount to be reduced
"hes":"" -- The hesitation amount to be reduced
}
]

# Delegation adjustment, delegationShouldNotHaveAmount for the delegation amount that should not be there, the fields in the following table need to be adjusted

1. node table:
   total_value [Total valid pledge delegations (AAA)] - delegationShouldNotHaveAmount
   stat_delegate_value [effective delegation amount (AAA)] - delegationShouldNotHaveAmount (valid delegation is deducted first)
   stat_delegate_released [Delegation amount to be withdrawn (AAA)] - delegationShouldNotHaveAmount (if the effective delegation is not enough, then the deduction will be deducted to be withdrawn)

2. Staking table:
   stat_delegate_hes [unlocked delegation (AAA)] - delegationShouldNotHaveAmount (reduce hesitation first)
   stat_delegate_locked [Locked delegation (AAA)] - delegationShouldNotHaveAmount' (reduce the lock if you hesitate not enough)
   stat_delegate_released [Delegation to be extracted (AAA)] - delegationShouldNotHaveAmount'' (if the lock is not enough, then reduce it to be extracted)

3. Delegation table:
   delegate_hes [unlocked delegation amount (AAA)] - delegationShouldNotHaveAmount (reduce hesitation first)
   delegate_locked [Locked delegation amount (AAA)] - delegationShouldNotHaveAmount' (reduce the lock if you are not hesitant enough)
   delegate_released [Amount to be withdrawn (AAA)] - delegationShouldNotHaveAmount'' (if the lock is not enough, then reduce the amount to be withdrawn)

   After the amount is deducted, if delegate_hes+delegate_locked+delegate_released = 0, the delegation becomes history.

4. Address table: The following fields are updated by the existing AddressUpdateTask task regular statistics related table, so there is no need to modify this table.
   delegate_value [Amount of delegation (AAA)]
   delegate_hes【Unlocked delegate amount (AAA)】
   delegate_locked【Locked delegate amount (AAA)】
   delegate_released [amount to be withdrawn (AAA)]


# Pledge adjustment, for the staking amount that should not be stakingShouldNotHaveAmount, the fields in the following table need to be adjusted

1. node table:
   total_value [Total effective staking delegation (AAA)] - stakingShouldNotHaveAmount
   staking_hes [Staking deposit during the hesitation period (AAA)] - stakingShouldNotHaveAmount (reduce the hesitation first)
   staking_locked [Pledge deposit during the locking period (AAA)] - stakingShouldNotHaveAmount' (reduce the lock if you hesitate not enough)
   staking_reduction [refunding pledge (AAA)] - stakingShouldNotHaveAmount'' (if the lock is not enough, then reduce it and wait for withdrawal)

2. Staking table:
   staking_hes [Staking deposit during the hesitation period (AAA)] - stakingShouldNotHaveAmount (reduce the hesitation first)
   staking_locked [Pledge deposit during the locking period (AAA)] - stakingShouldNotHaveAmount' (reduce the lock if you hesitate not enough)
   staking_reduction [refunding pledge (AAA)] - stakingShouldNotHaveAmount'' (if the lock is not enough, then reduce it and wait for withdrawal)

   After the amount is deducted, if staking_hes+staking_locked < staking threshold, the node executes the exit logic and the status is directly set to exited.

3. Address table: The following fields are updated by the existing AddressUpdateTask task regular statistics related table, so there is no need to modify this table.
   staking_value [Amount of pledge (AAA)]
   redeemed_value [Pledge amount in redemption (AAA)]
