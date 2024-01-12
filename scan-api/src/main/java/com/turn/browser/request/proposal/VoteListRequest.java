package com.turn.browser.request.proposal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.turn.browser.request.PageReq;

/**
 * Voting request object
 */
public class VoteListRequest extends PageReq{
	@NotBlank(message = "{proposalHash not null}")
	@Size(min = 60,max = 66)
    private String proposalHash;
    
    private String option;

	public String getProposalHash() {
		return proposalHash;
	}

	public void setProposalHash(String proposalHash) {
		this.proposalHash = proposalHash;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}
    
}