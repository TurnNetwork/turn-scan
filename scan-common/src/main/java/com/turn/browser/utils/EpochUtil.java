package com.turn.browser.utils;

import com.turn.browser.exception.BlockNumberException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * @Description: Epoch calculation tool
 */
public class EpochUtil {
    private EpochUtil(){}
    /**
     * Take the period and round up
     * @param blockNumber current block number
     * @param blockCountPerEpoch The number of blocks per cycle
     * @return
     */
    public static BigInteger getEpoch(BigInteger blockNumber, BigInteger blockCountPerEpoch) throws BlockNumberException {
        if(BigInteger.ZERO.compareTo(blockCountPerEpoch)>=0) throw new BlockNumberException("周期区块数必须大于0");
        if(BigInteger.ZERO.compareTo(blockNumber)>0) return BigInteger.ZERO;
        BigDecimal epoch = new BigDecimal(blockNumber).divide(new BigDecimal(blockCountPerEpoch),0, RoundingMode.CEILING);
        return epoch.toBigInteger();
    }
    /**
     * Get the last block number of the previous cycle
     * @param blockNumber current block number
     * @param blockCountPerEpoch The number of blocks per cycle
     * @return
     */
    public static BigInteger getPreEpochLastBlockNumber(BigInteger blockNumber, BigInteger blockCountPerEpoch) throws BlockNumberException {
        BigInteger curEpoch = getEpoch(blockNumber,blockCountPerEpoch);
        if(BigInteger.ZERO.compareTo(curEpoch)>0) throw new BlockNumberException("当前周期为("+curEpoch+"),没有上一周期");
        if(BigInteger.ZERO.compareTo(curEpoch)==0) return curEpoch;
        BigInteger prevEpoch = curEpoch.subtract(BigInteger.ONE);
        return prevEpoch.multiply(blockCountPerEpoch);
    }

    /**
     * Get the last block number of the current cycle
     * @param blockNumber current block number
     * @param blockCountPerEpoch The number of blocks per cycle
     * @return
     */
    public static BigInteger getCurEpochLastBlockNumber(BigInteger blockNumber, BigInteger blockCountPerEpoch) throws BlockNumberException {
        BigInteger curEpoch = getEpoch(blockNumber,blockCountPerEpoch);
        return curEpoch.multiply(blockCountPerEpoch);
    }
}
