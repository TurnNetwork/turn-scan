package com.turn.browser.service.elasticsearch.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Collection;

public class ESSimpleExpression {
    private String fieldName; //property name
    private Object value; //corresponding value
    private Collection<Object> values; //corresponding values
    private ESCriterion.Operator operator; //Operator
    private Object from;
    private Object to;

    protected  ESSimpleExpression(String fieldName, Object value, ESCriterion.Operator operator) {
        this.fieldName = fieldName;
        this.value = value;
        this.operator = operator;
    }

    protected  ESSimpleExpression(String value, ESCriterion.Operator operator) {
        this.value = value;
        this.operator = operator;
    }

    protected ESSimpleExpression(String fieldName, Collection<Object> values) {
        this.fieldName = fieldName;
        this.values = values;
        this.operator = ESCriterion.Operator.TERMS;
    }

    protected ESSimpleExpression(String fieldName, Object from, Object to) {
        this.fieldName = fieldName;
        this.from = from;
        this.to = to;
        this.operator = ESCriterion.Operator.RANGE;
    }

    public QueryBuilder toBuilder() {
        QueryBuilder qb = null;
        switch (operator) {
            case TERM:
                qb = QueryBuilders.termQuery(fieldName, value);
                break;
            case TERMS:
                qb = QueryBuilders.termsQuery(fieldName, values);
                break;
            case RANGE:
                qb = QueryBuilders.rangeQuery(fieldName).from(from).to(to).includeLower(true).includeUpper(true);
                break;
            case FUZZY:
                qb = QueryBuilders.fuzzyQuery(fieldName, value);
                break;
            case QUERY_STRING:
                qb = QueryBuilders.queryStringQuery(value.toString());
                break;
		default:
			break;
        }
        return qb;
    }
}
