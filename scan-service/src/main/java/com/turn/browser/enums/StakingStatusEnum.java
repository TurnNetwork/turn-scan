package com.turn.browser.enums;

import com.turn.browser.bean.CustomStaking;

/**
 * Returns the front-end validator status enumeration
 */
public enum StakingStatusEnum {
	ALL("all", null),//all
	VERIFYING("verifying" ,6),//Consensus is medium
	CANDIDATE("candidate" ,1),//candidate
	ACTIVE("active", 2),//Active
	BLOCK("block", 3),//The block is being generated
	EXITING("exiting",4),//Exiting
	EXITED("exited",5),//Exited
	LOCKED("locked",7);//Locked

	private String name;
	
	private Integer code;
	
	StakingStatusEnum(String name, Integer code) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public Integer getCode() {
		return code;
	}

	/**
	 * Convert to the corresponding front-end state according to the data state
	 * @method getCodeByStatus
	 */
	public static Integer getCodeByStatus(Integer status, Integer isConsensus, Integer isSetting) {
		if(CustomStaking.StatusEnum.CANDIDATE.getCode() == status) {
			if(CustomStaking.YesNoEnum.YES.getCode() == isConsensus) {
                /** When the status is both candidate and in the consensus cycle, the node is considered to be in consensus*/
				return StakingStatusEnum.VERIFYING.getCode();
			}
			if(CustomStaking.YesNoEnum.YES.getCode() == isSetting) {
				/** When the status is both candidate and during the settlement cycle, the node is considered active*/
				return StakingStatusEnum.ACTIVE.getCode();
			}
			return StakingStatusEnum.CANDIDATE.getCode();
		}
		if(CustomStaking.StatusEnum.EXITING.getCode() == status) {
			if(CustomStaking.YesNoEnum.YES.getCode() == isSetting) {
				/** When the status is exiting and during the settlement cycle, the node is considered active*/
				return StakingStatusEnum.ACTIVE.getCode();
			}
			return StakingStatusEnum.EXITING.getCode();
		}
		if(CustomStaking.StatusEnum.EXITED.getCode() == status) {
			return StakingStatusEnum.EXITED.getCode();
		}
		/**Lock status*/
		if(CustomStaking.StatusEnum.LOCKED.getCode() == status) {
			return StakingStatusEnum.LOCKED.getCode();
		}
		return null;
	}
	
}
