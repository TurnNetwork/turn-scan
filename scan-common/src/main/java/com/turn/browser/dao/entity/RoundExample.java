package com.turn.browser.dao.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RoundExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RoundExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andGameIdIsNull() {
            addCriterion("game_id is null");
            return (Criteria) this;
        }

        public Criteria andGameIdIsNotNull() {
            addCriterion("game_id is not null");
            return (Criteria) this;
        }

        public Criteria andGameIdEqualTo(Long value) {
            addCriterion("game_id =", value, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdNotEqualTo(Long value) {
            addCriterion("game_id <>", value, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdGreaterThan(Long value) {
            addCriterion("game_id >", value, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdGreaterThanOrEqualTo(Long value) {
            addCriterion("game_id >=", value, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdLessThan(Long value) {
            addCriterion("game_id <", value, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdLessThanOrEqualTo(Long value) {
            addCriterion("game_id <=", value, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdIn(List<Long> values) {
            addCriterion("game_id in", values, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdNotIn(List<Long> values) {
            addCriterion("game_id not in", values, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdBetween(Long value1, Long value2) {
            addCriterion("game_id between", value1, value2, "gameId");
            return (Criteria) this;
        }

        public Criteria andGameIdNotBetween(Long value1, Long value2) {
            addCriterion("game_id not between", value1, value2, "gameId");
            return (Criteria) this;
        }

        public Criteria andRoundIdIsNull() {
            addCriterion("round_id is null");
            return (Criteria) this;
        }

        public Criteria andRoundIdIsNotNull() {
            addCriterion("round_id is not null");
            return (Criteria) this;
        }

        public Criteria andRoundIdEqualTo(Long value) {
            addCriterion("round_id =", value, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdNotEqualTo(Long value) {
            addCriterion("round_id <>", value, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdGreaterThan(Long value) {
            addCriterion("round_id >", value, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdGreaterThanOrEqualTo(Long value) {
            addCriterion("round_id >=", value, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdLessThan(Long value) {
            addCriterion("round_id <", value, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdLessThanOrEqualTo(Long value) {
            addCriterion("round_id <=", value, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdIn(List<Long> values) {
            addCriterion("round_id in", values, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdNotIn(List<Long> values) {
            addCriterion("round_id not in", values, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdBetween(Long value1, Long value2) {
            addCriterion("round_id between", value1, value2, "roundId");
            return (Criteria) this;
        }

        public Criteria andRoundIdNotBetween(Long value1, Long value2) {
            addCriterion("round_id not between", value1, value2, "roundId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdIsNull() {
            addCriterion("bubble_id is null");
            return (Criteria) this;
        }

        public Criteria andBubbleIdIsNotNull() {
            addCriterion("bubble_id is not null");
            return (Criteria) this;
        }

        public Criteria andBubbleIdEqualTo(Long value) {
            addCriterion("bubble_id =", value, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdNotEqualTo(Long value) {
            addCriterion("bubble_id <>", value, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdGreaterThan(Long value) {
            addCriterion("bubble_id >", value, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdGreaterThanOrEqualTo(Long value) {
            addCriterion("bubble_id >=", value, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdLessThan(Long value) {
            addCriterion("bubble_id <", value, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdLessThanOrEqualTo(Long value) {
            addCriterion("bubble_id <=", value, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdIn(List<Long> values) {
            addCriterion("bubble_id in", values, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdNotIn(List<Long> values) {
            addCriterion("bubble_id not in", values, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdBetween(Long value1, Long value2) {
            addCriterion("bubble_id between", value1, value2, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andBubbleIdNotBetween(Long value1, Long value2) {
            addCriterion("bubble_id not between", value1, value2, "bubbleId");
            return (Criteria) this;
        }

        public Criteria andCreatorIsNull() {
            addCriterion("creator is null");
            return (Criteria) this;
        }

        public Criteria andCreatorIsNotNull() {
            addCriterion("creator is not null");
            return (Criteria) this;
        }

        public Criteria andCreatorEqualTo(String value) {
            addCriterion("creator =", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotEqualTo(String value) {
            addCriterion("creator <>", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorGreaterThan(String value) {
            addCriterion("creator >", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorGreaterThanOrEqualTo(String value) {
            addCriterion("creator >=", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLessThan(String value) {
            addCriterion("creator <", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLessThanOrEqualTo(String value) {
            addCriterion("creator <=", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLike(String value) {
            addCriterion("creator like", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotLike(String value) {
            addCriterion("creator not like", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorIn(List<String> values) {
            addCriterion("creator in", values, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotIn(List<String> values) {
            addCriterion("creator not in", values, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorBetween(String value1, String value2) {
            addCriterion("creator between", value1, value2, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotBetween(String value1, String value2) {
            addCriterion("creator not between", value1, value2, "creator");
            return (Criteria) this;
        }

        public Criteria andTokenAddressIsNull() {
            addCriterion("token_address is null");
            return (Criteria) this;
        }

        public Criteria andTokenAddressIsNotNull() {
            addCriterion("token_address is not null");
            return (Criteria) this;
        }

        public Criteria andTokenAddressEqualTo(String value) {
            addCriterion("token_address =", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressNotEqualTo(String value) {
            addCriterion("token_address <>", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressGreaterThan(String value) {
            addCriterion("token_address >", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressGreaterThanOrEqualTo(String value) {
            addCriterion("token_address >=", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressLessThan(String value) {
            addCriterion("token_address <", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressLessThanOrEqualTo(String value) {
            addCriterion("token_address <=", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressLike(String value) {
            addCriterion("token_address like", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressNotLike(String value) {
            addCriterion("token_address not like", value, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressIn(List<String> values) {
            addCriterion("token_address in", values, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressNotIn(List<String> values) {
            addCriterion("token_address not in", values, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressBetween(String value1, String value2) {
            addCriterion("token_address between", value1, value2, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenAddressNotBetween(String value1, String value2) {
            addCriterion("token_address not between", value1, value2, "tokenAddress");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolIsNull() {
            addCriterion("token_symbol is null");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolIsNotNull() {
            addCriterion("token_symbol is not null");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolEqualTo(String value) {
            addCriterion("token_symbol =", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolNotEqualTo(String value) {
            addCriterion("token_symbol <>", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolGreaterThan(String value) {
            addCriterion("token_symbol >", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolGreaterThanOrEqualTo(String value) {
            addCriterion("token_symbol >=", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolLessThan(String value) {
            addCriterion("token_symbol <", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolLessThanOrEqualTo(String value) {
            addCriterion("token_symbol <=", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolLike(String value) {
            addCriterion("token_symbol like", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolNotLike(String value) {
            addCriterion("token_symbol not like", value, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolIn(List<String> values) {
            addCriterion("token_symbol in", values, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolNotIn(List<String> values) {
            addCriterion("token_symbol not in", values, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolBetween(String value1, String value2) {
            addCriterion("token_symbol between", value1, value2, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenSymbolNotBetween(String value1, String value2) {
            addCriterion("token_symbol not between", value1, value2, "tokenSymbol");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalIsNull() {
            addCriterion("token_decimal is null");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalIsNotNull() {
            addCriterion("token_decimal is not null");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalEqualTo(Integer value) {
            addCriterion("token_decimal =", value, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalNotEqualTo(Integer value) {
            addCriterion("token_decimal <>", value, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalGreaterThan(Integer value) {
            addCriterion("token_decimal >", value, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalGreaterThanOrEqualTo(Integer value) {
            addCriterion("token_decimal >=", value, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalLessThan(Integer value) {
            addCriterion("token_decimal <", value, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalLessThanOrEqualTo(Integer value) {
            addCriterion("token_decimal <=", value, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalIn(List<Integer> values) {
            addCriterion("token_decimal in", values, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalNotIn(List<Integer> values) {
            addCriterion("token_decimal not in", values, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalBetween(Integer value1, Integer value2) {
            addCriterion("token_decimal between", value1, value2, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenDecimalNotBetween(Integer value1, Integer value2) {
            addCriterion("token_decimal not between", value1, value2, "tokenDecimal");
            return (Criteria) this;
        }

        public Criteria andTokenRpcIsNull() {
            addCriterion("token_rpc is null");
            return (Criteria) this;
        }

        public Criteria andTokenRpcIsNotNull() {
            addCriterion("token_rpc is not null");
            return (Criteria) this;
        }

        public Criteria andTokenRpcEqualTo(String value) {
            addCriterion("token_rpc =", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcNotEqualTo(String value) {
            addCriterion("token_rpc <>", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcGreaterThan(String value) {
            addCriterion("token_rpc >", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcGreaterThanOrEqualTo(String value) {
            addCriterion("token_rpc >=", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcLessThan(String value) {
            addCriterion("token_rpc <", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcLessThanOrEqualTo(String value) {
            addCriterion("token_rpc <=", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcLike(String value) {
            addCriterion("token_rpc like", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcNotLike(String value) {
            addCriterion("token_rpc not like", value, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcIn(List<String> values) {
            addCriterion("token_rpc in", values, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcNotIn(List<String> values) {
            addCriterion("token_rpc not in", values, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcBetween(String value1, String value2) {
            addCriterion("token_rpc between", value1, value2, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andTokenRpcNotBetween(String value1, String value2) {
            addCriterion("token_rpc not between", value1, value2, "tokenRpc");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}