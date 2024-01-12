package com.turn.browser.dao.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MicroNodeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MicroNodeExample() {
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

        public Criteria andNodeIdIsNull() {
            addCriterion("node_id is null");
            return (Criteria) this;
        }

        public Criteria andNodeIdIsNotNull() {
            addCriterion("node_id is not null");
            return (Criteria) this;
        }

        public Criteria andNodeIdEqualTo(String value) {
            addCriterion("node_id =", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotEqualTo(String value) {
            addCriterion("node_id <>", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdGreaterThan(String value) {
            addCriterion("node_id >", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdGreaterThanOrEqualTo(String value) {
            addCriterion("node_id >=", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLessThan(String value) {
            addCriterion("node_id <", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLessThanOrEqualTo(String value) {
            addCriterion("node_id <=", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLike(String value) {
            addCriterion("node_id like", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotLike(String value) {
            addCriterion("node_id not like", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdIn(List<String> values) {
            addCriterion("node_id in", values, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotIn(List<String> values) {
            addCriterion("node_id not in", values, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdBetween(String value1, String value2) {
            addCriterion("node_id between", value1, value2, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotBetween(String value1, String value2) {
            addCriterion("node_id not between", value1, value2, "nodeId");
            return (Criteria) this;
        }

        public Criteria andAmountIsNull() {
            addCriterion("amount is null");
            return (Criteria) this;
        }

        public Criteria andAmountIsNotNull() {
            addCriterion("amount is not null");
            return (Criteria) this;
        }

        public Criteria andAmountEqualTo(BigDecimal value) {
            addCriterion("amount =", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotEqualTo(BigDecimal value) {
            addCriterion("amount <>", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountGreaterThan(BigDecimal value) {
            addCriterion("amount >", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("amount >=", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountLessThan(BigDecimal value) {
            addCriterion("amount <", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("amount <=", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountIn(List<BigDecimal> values) {
            addCriterion("amount in", values, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotIn(List<BigDecimal> values) {
            addCriterion("amount not in", values, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("amount between", value1, value2, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("amount not between", value1, value2, "amount");
            return (Criteria) this;
        }

        public Criteria andOperationAddrIsNull() {
            addCriterion("operation_addr is null");
            return (Criteria) this;
        }

        public Criteria andOperationAddrIsNotNull() {
            addCriterion("operation_addr is not null");
            return (Criteria) this;
        }

        public Criteria andOperationAddrEqualTo(String value) {
            addCriterion("operation_addr =", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrNotEqualTo(String value) {
            addCriterion("operation_addr <>", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrGreaterThan(String value) {
            addCriterion("operation_addr >", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrGreaterThanOrEqualTo(String value) {
            addCriterion("operation_addr >=", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrLessThan(String value) {
            addCriterion("operation_addr <", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrLessThanOrEqualTo(String value) {
            addCriterion("operation_addr <=", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrLike(String value) {
            addCriterion("operation_addr like", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrNotLike(String value) {
            addCriterion("operation_addr not like", value, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrIn(List<String> values) {
            addCriterion("operation_addr in", values, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrNotIn(List<String> values) {
            addCriterion("operation_addr not in", values, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrBetween(String value1, String value2) {
            addCriterion("operation_addr between", value1, value2, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andOperationAddrNotBetween(String value1, String value2) {
            addCriterion("operation_addr not between", value1, value2, "operationAddr");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryIsNull() {
            addCriterion("beneficiary is null");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryIsNotNull() {
            addCriterion("beneficiary is not null");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryEqualTo(String value) {
            addCriterion("beneficiary =", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryNotEqualTo(String value) {
            addCriterion("beneficiary <>", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryGreaterThan(String value) {
            addCriterion("beneficiary >", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryGreaterThanOrEqualTo(String value) {
            addCriterion("beneficiary >=", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryLessThan(String value) {
            addCriterion("beneficiary <", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryLessThanOrEqualTo(String value) {
            addCriterion("beneficiary <=", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryLike(String value) {
            addCriterion("beneficiary like", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryNotLike(String value) {
            addCriterion("beneficiary not like", value, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryIn(List<String> values) {
            addCriterion("beneficiary in", values, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryNotIn(List<String> values) {
            addCriterion("beneficiary not in", values, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryBetween(String value1, String value2) {
            addCriterion("beneficiary between", value1, value2, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andBeneficiaryNotBetween(String value1, String value2) {
            addCriterion("beneficiary not between", value1, value2, "beneficiary");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andDetailsIsNull() {
            addCriterion("details is null");
            return (Criteria) this;
        }

        public Criteria andDetailsIsNotNull() {
            addCriterion("details is not null");
            return (Criteria) this;
        }

        public Criteria andDetailsEqualTo(String value) {
            addCriterion("details =", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsNotEqualTo(String value) {
            addCriterion("details <>", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsGreaterThan(String value) {
            addCriterion("details >", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsGreaterThanOrEqualTo(String value) {
            addCriterion("details >=", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsLessThan(String value) {
            addCriterion("details <", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsLessThanOrEqualTo(String value) {
            addCriterion("details <=", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsLike(String value) {
            addCriterion("details like", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsNotLike(String value) {
            addCriterion("details not like", value, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsIn(List<String> values) {
            addCriterion("details in", values, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsNotIn(List<String> values) {
            addCriterion("details not in", values, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsBetween(String value1, String value2) {
            addCriterion("details between", value1, value2, "details");
            return (Criteria) this;
        }

        public Criteria andDetailsNotBetween(String value1, String value2) {
            addCriterion("details not between", value1, value2, "details");
            return (Criteria) this;
        }

        public Criteria andElectronUriIsNull() {
            addCriterion("electron_uri is null");
            return (Criteria) this;
        }

        public Criteria andElectronUriIsNotNull() {
            addCriterion("electron_uri is not null");
            return (Criteria) this;
        }

        public Criteria andElectronUriEqualTo(String value) {
            addCriterion("electron_uri =", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriNotEqualTo(String value) {
            addCriterion("electron_uri <>", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriGreaterThan(String value) {
            addCriterion("electron_uri >", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriGreaterThanOrEqualTo(String value) {
            addCriterion("electron_uri >=", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriLessThan(String value) {
            addCriterion("electron_uri <", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriLessThanOrEqualTo(String value) {
            addCriterion("electron_uri <=", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriLike(String value) {
            addCriterion("electron_uri like", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriNotLike(String value) {
            addCriterion("electron_uri not like", value, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriIn(List<String> values) {
            addCriterion("electron_uri in", values, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriNotIn(List<String> values) {
            addCriterion("electron_uri not in", values, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriBetween(String value1, String value2) {
            addCriterion("electron_uri between", value1, value2, "electronUri");
            return (Criteria) this;
        }

        public Criteria andElectronUriNotBetween(String value1, String value2) {
            addCriterion("electron_uri not between", value1, value2, "electronUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriIsNull() {
            addCriterion("p2p_uri is null");
            return (Criteria) this;
        }

        public Criteria andP2pUriIsNotNull() {
            addCriterion("p2p_uri is not null");
            return (Criteria) this;
        }

        public Criteria andP2pUriEqualTo(String value) {
            addCriterion("p2p_uri =", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriNotEqualTo(String value) {
            addCriterion("p2p_uri <>", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriGreaterThan(String value) {
            addCriterion("p2p_uri >", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriGreaterThanOrEqualTo(String value) {
            addCriterion("p2p_uri >=", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriLessThan(String value) {
            addCriterion("p2p_uri <", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriLessThanOrEqualTo(String value) {
            addCriterion("p2p_uri <=", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriLike(String value) {
            addCriterion("p2p_uri like", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriNotLike(String value) {
            addCriterion("p2p_uri not like", value, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriIn(List<String> values) {
            addCriterion("p2p_uri in", values, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriNotIn(List<String> values) {
            addCriterion("p2p_uri not in", values, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriBetween(String value1, String value2) {
            addCriterion("p2p_uri between", value1, value2, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andP2pUriNotBetween(String value1, String value2) {
            addCriterion("p2p_uri not between", value1, value2, "p2pUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriIsNull() {
            addCriterion("rpc_uri is null");
            return (Criteria) this;
        }

        public Criteria andRpcUriIsNotNull() {
            addCriterion("rpc_uri is not null");
            return (Criteria) this;
        }

        public Criteria andRpcUriEqualTo(String value) {
            addCriterion("rpc_uri =", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriNotEqualTo(String value) {
            addCriterion("rpc_uri <>", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriGreaterThan(String value) {
            addCriterion("rpc_uri >", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriGreaterThanOrEqualTo(String value) {
            addCriterion("rpc_uri >=", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriLessThan(String value) {
            addCriterion("rpc_uri <", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriLessThanOrEqualTo(String value) {
            addCriterion("rpc_uri <=", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriLike(String value) {
            addCriterion("rpc_uri like", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriNotLike(String value) {
            addCriterion("rpc_uri not like", value, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriIn(List<String> values) {
            addCriterion("rpc_uri in", values, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriNotIn(List<String> values) {
            addCriterion("rpc_uri not in", values, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriBetween(String value1, String value2) {
            addCriterion("rpc_uri between", value1, value2, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andRpcUriNotBetween(String value1, String value2) {
            addCriterion("rpc_uri not between", value1, value2, "rpcUri");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(String value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(String value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(String value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(String value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(String value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(String value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLike(String value) {
            addCriterion("version like", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotLike(String value) {
            addCriterion("version not like", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<String> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<String> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(String value1, String value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(String value1, String value2) {
            addCriterion("version not between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andIsOperatorIsNull() {
            addCriterion("is_operator is null");
            return (Criteria) this;
        }

        public Criteria andIsOperatorIsNotNull() {
            addCriterion("is_operator is not null");
            return (Criteria) this;
        }

        public Criteria andIsOperatorEqualTo(Integer value) {
            addCriterion("is_operator =", value, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorNotEqualTo(Integer value) {
            addCriterion("is_operator <>", value, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorGreaterThan(Integer value) {
            addCriterion("is_operator >", value, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_operator >=", value, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorLessThan(Integer value) {
            addCriterion("is_operator <", value, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorLessThanOrEqualTo(Integer value) {
            addCriterion("is_operator <=", value, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorIn(List<Integer> values) {
            addCriterion("is_operator in", values, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorNotIn(List<Integer> values) {
            addCriterion("is_operator not in", values, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorBetween(Integer value1, Integer value2) {
            addCriterion("is_operator between", value1, value2, "isOperator");
            return (Criteria) this;
        }

        public Criteria andIsOperatorNotBetween(Integer value1, Integer value2) {
            addCriterion("is_operator not between", value1, value2, "isOperator");
            return (Criteria) this;
        }

        public Criteria andNodeStatusIsNull() {
            addCriterion("node_status is null");
            return (Criteria) this;
        }

        public Criteria andNodeStatusIsNotNull() {
            addCriterion("node_status is not null");
            return (Criteria) this;
        }

        public Criteria andNodeStatusEqualTo(Integer value) {
            addCriterion("node_status =", value, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusNotEqualTo(Integer value) {
            addCriterion("node_status <>", value, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusGreaterThan(Integer value) {
            addCriterion("node_status >", value, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("node_status >=", value, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusLessThan(Integer value) {
            addCriterion("node_status <", value, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusLessThanOrEqualTo(Integer value) {
            addCriterion("node_status <=", value, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusIn(List<Integer> values) {
            addCriterion("node_status in", values, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusNotIn(List<Integer> values) {
            addCriterion("node_status not in", values, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusBetween(Integer value1, Integer value2) {
            addCriterion("node_status between", value1, value2, "nodeStatus");
            return (Criteria) this;
        }

        public Criteria andNodeStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("node_status not between", value1, value2, "nodeStatus");
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

        public Criteria andBubbleCreatorIsNull() {
            addCriterion("bubble_creator is null");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorIsNotNull() {
            addCriterion("bubble_creator is not null");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorEqualTo(String value) {
            addCriterion("bubble_creator =", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorNotEqualTo(String value) {
            addCriterion("bubble_creator <>", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorGreaterThan(String value) {
            addCriterion("bubble_creator >", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorGreaterThanOrEqualTo(String value) {
            addCriterion("bubble_creator >=", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorLessThan(String value) {
            addCriterion("bubble_creator <", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorLessThanOrEqualTo(String value) {
            addCriterion("bubble_creator <=", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorLike(String value) {
            addCriterion("bubble_creator like", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorNotLike(String value) {
            addCriterion("bubble_creator not like", value, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorIn(List<String> values) {
            addCriterion("bubble_creator in", values, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorNotIn(List<String> values) {
            addCriterion("bubble_creator not in", values, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorBetween(String value1, String value2) {
            addCriterion("bubble_creator between", value1, value2, "bubbleCreator");
            return (Criteria) this;
        }

        public Criteria andBubbleCreatorNotBetween(String value1, String value2) {
            addCriterion("bubble_creator not between", value1, value2, "bubbleCreator");
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