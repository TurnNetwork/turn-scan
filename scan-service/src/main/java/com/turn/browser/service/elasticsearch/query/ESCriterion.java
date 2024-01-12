package com.turn.browser.service.elasticsearch.query;

import org.elasticsearch.index.query.QueryBuilder;
import java.util.List;

/**
 *es query interface
 */
public interface ESCriterion {
	/**
	 * Operation enumeration
	 */
	enum Operator {
		TERM, TERMS, RANGE, FUZZY, QUERY_STRING, MISSING
	}

	List<QueryBuilder> listBuilders();
}