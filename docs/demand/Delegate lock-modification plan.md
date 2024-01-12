### Database table modification
1.

a. The governance parameter warehousing service adds the number of undelegation freezing cycles: un_delegate_freeze_duration
b. New field un_delegate_end_block is added to the delegation table [delegation effective block number]
c. Add the [frozen] field delegate_frozen to the delegation table
d. Add the [to be redeemed] field delegate_redeem to the delegation table
e. Added [Invalid Hesitation] field delegate_hes_invalid to the delegation table
f. Added [Frozen Delegation] field delegate_frozen to the address table
g. Add the [to be redeemed] field delegate_redeem to the address table


    Amount-related fields of the modified order form:
    delegate_hes effective hesitation (AAA) -- the newly added commission amount after the commission transaction is successful
    delegate_hes_invalid invalidation (AAA) -- the amount of [delegate_hes] is moved to [delegate_hes_invalid] due to node exit
    delegate_locked effective lock (AAA) -- the amount in the hesitation period becomes the effective locked delegation amount after the specified settlement period
    delegate_locked_invalid invalid lock (AAA) -- the amount of [delegate_locked] is moved to [delegate_locked_invalid] due to node exit
    delegate_frozen is frozen (AAA) -- The amount in [delegate_locked] or [delegate_released] is moved to [delegate_frozen] due to the user's application to release the delegation.
    delegate_redeem to be redeemed (AAA) -- due to the end of the freeze, the amount of [delegate_frozen] was moved to [delegate_redeem]
Business logic modification:
1. Node exit: (3 days)
   a. Commission record:
   [delegate_hes] moved to [delegate_hes_invalid] -- the hesitation period delegation becomes an invalid hesitation delegation
   [delegate_locked] moved to [delegate_locked_invalid] -- the lock period delegation becomes the invalid lock delegation
   b. Pledge records:
   [stat_delegate_released (renamed to stat_delegate_invalid - invalid delegation)] plus delegated ([delegate_hes] + [delegate_locked]) -- keep the previous version logic,
   [stat_delegate_hes] minus the delegated [delegate_hes] -- maintain the logic of the previous version,
   [stat_delegate_locked] minus the delegated [delegate_locked] -- maintain the logic of the previous version;
   c. Node record:
   [stat_delegate_released (renamed to stat_delegate_invalid - invalid delegation)] plus delegated ([delegate_hes] + [delegate_locked]) -- maintain the previous version logic;
   [stat_delegate_value] minus the delegate ([delegate_hes] + [delegate_locked])
   d. Address record:
   [delegate_hes] minus the delegated [delegate_hes] -- maintain the logic of the previous version,
   [delegate_locked] minus the delegated [delegate_locked] -- maintain the logic of the previous version,
   [delegate_released (renamed to stat_delegate_invalid - invalid delegation)] plus delegated [delegate_locked] -- maintain the previous version logic;

2. Release of delegation: (4 days)
   a. Calculate & record [Delegation effective block number: un_delegate_end_block] = ([Number of settlement cycles in which delegation is decommissioned] + [Number of frozen settlement cycles for decommissioning]) * [Number of blocks in each settlement cycle]);
   b. Entrusted to record the real release amount [realRefundAmount]: the hesitation period will be deducted first, and the lock-up will be deducted if the hesitation period is not enough.
      b1. The amount deducted from [delegate_hes] or [delegate_hes_invalid] will be credited directly to the account
      b2. If you hesitate not to deduct enough, then deduct the amount of [delegate_locked] or [delegate_locked_invalid] to [delegate_frozen]

   c. Pledge records:
    c1. If the node has not exited: `stat_delegate_hes` = `stat_delegate_hes` - realRefundAmount hesitation period part,
    c2. If the node does not exit: `stat_delegate_locked` = `stat_delegate_locked` - realRefundAmount lock part
    c3. If the node has exited: `stat_delegate_released` (renamed to stat_delegate_invalid - invalid delegation) = `stat_delegate_released` - realRefundAmount
    c4. New field stat_delegate_frozen = stat_delegate_frozen + realRefundAmount real frozen part

   d. Node record:
    d1, `total_value` = `total_value` - realRefundAmount
    d2. If the node does not exit: `stat_delegate_value` = `stat_delegate_value` - realRefundAmount
d3. If the node has exited: `stat_delegate_released` (renamed to stat_delegate_invalid - invalid delegation) = `stat_delegate_released` - realRefundAmount
d4. New field stat_delegate_frozen = stat_delegate_frozen + realRefundAmount real frozen part
d4. Delete the logic of receiving rewards and move the logic of receiving rewards to the redemption commission.
e. Address record:
e1. If the node has not exited: [delegate_hes]-realRefundAmount hesitation period part
e2. If the node does not exit: [delegate_locked] - realRefundAmount lock part
e3. If the node has exited: [delegate_released (renamed to stat_delegate_invalid - invalid delegation)] - realRefundAmount
e4. New field: delegate_frozen = stat_delegate_frozen + realRefundAmount real frozen part

    f. Commission record: If the amount of all the following fields is 0, the commission is set to history:
        delegate_hes valid hesitation (AAA)
        delegate_hes_invalid invalidation hesitation (AAA)
        delegate_locked effective lock (AAA)
        delegate_released invalid lock (AAA)
        delegate_frozen freezing (AAA)
        delegate_redeem to be redeemed (AAA)

3. Settlement cycle: (2 days)
   a. If the switching block number of the current settlement cycle is equal to the commissioned [un_delegate_end_block], that is, the commissioned amount is frozen, then the subsequent steps will be performed;
   b. Delegation record unfrozen amount unfrozenAmount: [delegate_frozen] is moved to [delegate_redeem], that is, [unfrozen amount] is moved to pending redemption;
   c. Staking and node records:
   c1, stat_delegate_redeem (new field) = stat_delegate_redeem + unfrozenAmount
   c2. stat_delegate_frozen = stat_delegate_frozen - unfrozenAmount

3. Redemption entrustment: (3 days)
   a. Delegation record: [delegate_redeem] is set to zero (because the underlying redemption transaction does not specify the redemption amount),
   b. Commission record: If the amount of all the following fields is 0, the commission is set to history:
   delegate_hes valid hesitation (AAA)
   delegate_hes_invalid invalidation hesitation (AAA)
   delegate_locked effective lock (AAA)
   delegate_released invalid lock (AAA)
   delegate_frozen freezing (AAA)
   delegate_redeem to be redeemed (AAA)
   c. Staking and node records:
   c1. stat_delegate_redeem (new field) = stat_delegate_redeem - delegated [delegate_redeem]
   c2. Execute the logic of receiving rewards: `have_dele_reward` = `have_dele_reward` + reward amount

   d. Address table:
   d1. Update the delegation reward field in StatisticsAddressConverter.

4. Mission (2 days)
   a. Address record: Add update logic for the [delegate_redeem] and [delegate_frozen] fields in AddressUpdateTask;
   b. The commission record becomes history and is migrated to history.

5. Front-end interface modification: (2 days)
   a./staking/delegationListByStaking
   b./address/details：
   c./staking/delegationListByAddress
   New return:
   delegate_frozen freezing (AAA)
   delegate_redeem to be redeemed (AAA)

   d./proposal/voteList
   Proposition 0: Click on the voter to jump to the voter’s address details