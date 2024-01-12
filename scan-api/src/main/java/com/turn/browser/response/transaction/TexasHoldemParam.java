package com.turn.browser.response.transaction;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class TexasHoldemParam {

    public String topics;

    public String name;

    public String contract;

    public String data;

    public BigInteger _tableId;

    public String _player;

    public BigInteger _bet;

    public BigInteger _smallBlind;

    public BigInteger _bigBlind;

    public String _tokenAddr;

    public String _round;

    public List<BigInteger> _board;

    public BigInteger _amount;

    public BigInteger _raise;

    public BigInteger _highestBet;

    public List<String> _playerAddrList;

    public BigInteger _numWinners;

    public List<String> _winnerList;

    public BigInteger _handRank;

    public BigInteger _revenuePerWinner;

}
