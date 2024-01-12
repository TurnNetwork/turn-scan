package com.turn.browser.dao.custommapper;

import com.turn.browser.bean.CustomVote;
import com.turn.browser.bean.CustomVoteProposal;

import java.util.List;

public interface CustomVoteMapper {
    List<CustomVote> selectAll ();

    CustomVoteProposal selectVotePropal ( String hash );
}
