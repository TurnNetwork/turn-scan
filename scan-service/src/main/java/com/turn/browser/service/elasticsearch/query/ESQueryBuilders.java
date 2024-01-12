package com.turn.browser.service.elasticsearch.query;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Query constructor
 */
public class ESQueryBuilders implements ESCriterion{

    private List<QueryBuilder> list = new ArrayList<>();

    /**
     * Function description: Term query
     * @param field field name
     * @param value value
     */
    public ESQueryBuilders term(String field, Object value) {
        list.add(new ESSimpleExpression (field, value, Operator.TERM).toBuilder());
        return this;
    }

    /**
     * Function description: Terms query
     * @param field field name
     * @param values ​​collection value
     */
    public ESQueryBuilders terms(String field, Collection<Object> values) {
        list.add(new ESSimpleExpression (field, values).toBuilder());
        return this;
    }

    /**
     * Function description: fuzzy query
     * @param field field name
     * @param value value
     */
    public ESQueryBuilders fuzzy(String field, Object value) {
        list.add(new ESSimpleExpression (field, value, Operator.FUZZY).toBuilder());
        return this;
    }

    /**
     * Function description: Range query
     * @param from starting value
     * @param to end value
     */
    public ESQueryBuilders range(String field, Object from, Object to) {
        list.add(new ESSimpleExpression (field, from, to).toBuilder());
        return this;
    }

    /**
     * Function description: Range query
     * @param queryString query statement
     */
    public ESQueryBuilders queryString(String queryString) {
        list.add(new ESSimpleExpression (queryString, Operator.QUERY_STRING).toBuilder());
        return this;
    }

    public List<QueryBuilder> listBuilders() {
        return list;
    }
}