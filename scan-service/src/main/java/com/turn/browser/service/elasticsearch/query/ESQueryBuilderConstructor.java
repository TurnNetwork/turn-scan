package com.turn.browser.service.elasticsearch.query;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * es query constructor manufacturing factory
 */
public class ESQueryBuilderConstructor {

    private int size = Integer.MAX_VALUE;

    private int from = 0;

    /**
     * Positive order sorting field
     */
    private String asc;

    /**
     * Sort fields in reverse order
     */
    private String desc;

    /**
     * Sorting field type
     */
    private String unmappedType;

    //Query condition container
    private List<ESCriterion> mustCriterions = new ArrayList<>();

    private List<ESCriterion> shouldCriterions = new ArrayList<>();

    private List<ESCriterion> mustNotCriterions = new ArrayList<>();

    private List<BoolQueryBuilder> queryMustBuilders = new ArrayList<>();

    private List<BoolQueryBuilder> queryShouldBuilders = new ArrayList<>();

    private String[] result;

    //Construct builder
    public QueryBuilder listBuilders() {
        int count = mustCriterions.size() + shouldCriterions.size() + mustNotCriterions.size() + queryMustBuilders.size() + queryShouldBuilders.size();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        QueryBuilder queryBuilder = null;

        if (count >= 1) {
            //must container
            if (!CollectionUtils.isEmpty(mustCriterions)) {
                for (ESCriterion criterion : mustCriterions) {
                    for (QueryBuilder builder : criterion.listBuilders()) {
                        queryBuilder = boolQueryBuilder.must(builder);
                    }
                }
            }
            //should container
            if (!CollectionUtils.isEmpty(shouldCriterions)) {
                for (ESCriterion criterion : shouldCriterions) {
                    for (QueryBuilder builder : criterion.listBuilders()) {
                        queryBuilder = boolQueryBuilder.should(builder);
                    }

                }
            }
            //must not container
            if (!CollectionUtils.isEmpty(mustNotCriterions)) {
                for (ESCriterion criterion : mustNotCriterions) {
                    for (QueryBuilder builder : criterion.listBuilders()) {
                        queryBuilder = boolQueryBuilder.mustNot(builder);
                    }
                }
            }

            if (!CollectionUtils.isEmpty(queryMustBuilders)) {
                for (BoolQueryBuilder boolQueryBuilder2 : queryMustBuilders) {
                    queryBuilder = boolQueryBuilder.must(boolQueryBuilder2);
                }
            }

            if (!CollectionUtils.isEmpty(queryShouldBuilders)) {
                for (BoolQueryBuilder boolQueryBuilder2 : queryShouldBuilders) {
                    queryBuilder = boolQueryBuilder.should(boolQueryBuilder2);
                }
            }
            return queryBuilder;
        } else {
            return null;
        }
    }

    /**
     * Add simple and conditional expressions
     */
    public ESQueryBuilderConstructor must(ESCriterion criterion) {
        if (criterion != null) {
            mustCriterions.add(criterion);
        }
        return this;
    }

    /**
     * Add simple or conditional expressions
     */
    public ESQueryBuilderConstructor should(ESCriterion criterion) {
        if (criterion != null) {
            shouldCriterions.add(criterion);
        }
        return this;
    }

    /**
     * Add simple conditional expression
     */
    public ESQueryBuilderConstructor mustNot(ESCriterion criterion) {
        if (criterion != null) {
            mustNotCriterions.add(criterion);
        }
        return this;
    }

    /**
     * Add complex conditional expressions
     */
    public ESQueryBuilderConstructor buildMust(BoolQueryBuilder boolQueryBuilder) {
        if (boolQueryBuilder != null) {
            queryMustBuilders.add(boolQueryBuilder);
        }
        return this;
    }
    /**
     * Add complex conditional expressions
     */
    public ESQueryBuilderConstructor buildShould(BoolQueryBuilder boolQueryBuilder) {
        if (boolQueryBuilder != null) {
            queryShouldBuilders.add(boolQueryBuilder);
        }
        return this;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getAsc() {
        return asc;
    }

    public void setAsc(String asc) {
        this.asc = asc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String[] getResult() {
        return result;
    }

    public void setResult(String[] result) {
        this.result = result;
    }

    public String getUnmappedType() {
        return unmappedType;
    }

    public void setUnmappedType(String unmappedType) {
        this.unmappedType = unmappedType;
    }

}