package com.turn.browser.response.staking;

/**
 * Lock validator list return object
 */
public class LockedStakingListResp extends AliveStakingListResp {
	private Long leaveTime; //Exit time
	private Long unlockBlockNum; //Estimated unlock block height
	public Long getLeaveTime() {
		return leaveTime;
	}
	public void setLeaveTime(Long leaveTime) {
		this.leaveTime = leaveTime;
	}

	public Long getUnlockBlockNum() {
		return unlockBlockNum;
	}

	public void setUnlockBlockNum(Long unlockBlockNum) {
		this.unlockBlockNum = unlockBlockNum;
	}
}
