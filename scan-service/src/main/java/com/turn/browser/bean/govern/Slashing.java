package com.turn.browser.bean.govern;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * @description: Modifiable penalty parameter configuration
 **/
@Builder
public class Slashing {
	private BigDecimal slashFractionDuplicateSign;
	private BigDecimal duplicateSignReportReward;
	private BigDecimal maxEvidenceAge;
	private BigDecimal slashBlocksReward;
	// Zero block generation threshold, if this number is reached within the specified time range, there will be a penalty
	private Integer zeroProduceNumberThreshold;
	// Description: Use N to represent the value set in the following fields, as explained below:
// After the last zero block, if there is another zero block in the next N consensus cycles, zero block information will be recorded when these N consensus cycles are completed.
	private Integer zeroProduceCumulativeTime;
	//Number of settlement cycles for zero block locking
	private Integer zeroProduceFreezeDuration;
	public BigDecimal getSlashFractionDuplicateSign() {
		return slashFractionDuplicateSign;
	}
	public void setSlashFractionDuplicateSign(BigDecimal slashFractionDuplicateSign) {
		this.slashFractionDuplicateSign = slashFractionDuplicateSign;
	}
	public BigDecimal getDuplicateSignReportReward() {
		return duplicateSignReportReward;
	}
	public void setDuplicateSignReportReward(BigDecimal duplicateSignReportReward) {
		this.duplicateSignReportReward = duplicateSignReportReward;
	}
	public BigDecimal getMaxEvidenceAge() {
		return maxEvidenceAge;
	}
	public void setMaxEvidenceAge(BigDecimal maxEvidenceAge) {
		this.maxEvidenceAge = maxEvidenceAge;
	}
	public BigDecimal getSlashBlocksReward() {
		return slashBlocksReward;
	}
	public void setSlashBlocksReward(BigDecimal slashBlocksReward) {
		this.slashBlocksReward = slashBlocksReward;
	}

	public Integer getZeroProduceNumberThreshold() {
		return zeroProduceNumberThreshold;
	}

	public void setZeroProduceNumberThreshold(Integer zeroProduceNumberThreshold) {
		this.zeroProduceNumberThreshold = zeroProduceNumberThreshold;
	}

	public Integer getZeroProduceCumulativeTime() {
		return zeroProduceCumulativeTime;
	}

	public void setZeroProduceCumulativeTime(Integer zeroProduceCumulativeTime) {
		this.zeroProduceCumulativeTime = zeroProduceCumulativeTime;
	}

	public Integer getZeroProduceFreezeDuration() {
		return zeroProduceFreezeDuration;
	}

	public void setZeroProduceFreezeDuration(Integer zeroProduceFreezeDuration) {
		this.zeroProduceFreezeDuration = zeroProduceFreezeDuration;
	}
}
