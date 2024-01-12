//package com.turn.browser.service.elasticsearch;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Repository;
//
///**
// */
//@Repository
//@Slf4j
//public class OldEsErc20TxRepository extends AbstractEsRepository {
//    @Override
//    public String getIndexName() {
//        return config.getErc20TxIndexName();
//    }
//    @Override
//    public String getTemplateFileName() {
//        return "erc20_list";
//    }
//
//    /*public TokenTxSummary groupContractTxCount(){
//        String equalCondition = "doc['from'].value == doc['to'].value";
//        TokenTxSummary equalSummary = groupContractTxCount(Arc20TxGroupTypeEnum.FROM,equalCondition);
//        String notEqualCondition = "doc['from'].value != doc['to'].value";
//        TokenTxSummary fromSummary = groupContractTxCount(Arc20TxGroupTypeEnum.FROM,notEqualCondition);
//        TokenTxSummary ttoSummary = groupContractTxCount(Arc20TxGroupTypeEnum.TO,notEqualCondition);
//
//
//        TokenTxSummary summary = new TokenTxSummary();
//        summary.setAddressTxCount(
//                equalSummary.getAddressTxCount()
//                        +fromSummary.getAddressTxCount()
//        );
//
//        Map<String,Long> summaryTxCountMap = summary.getAddressTxCountMap();
//        Arrays.asList(equalSummary,fromSummary,ttoSummary).forEach(subSummary->{
//            subSummary.getAddressTxCountMap().forEach((address,count)->{
//                Long sum = summaryTxCountMap.get(address);
//                if(sum==null) sum=0L;
//                sum +=count;
//                summaryTxCountMap.put(address,sum);
//            });
//        });
//
//        Map<String, TokenTxCount> contractTxCountMap = summary.getContractTxCountMap();
//        Arrays.asList(equalSummary,fromSummary,ttoSummary).forEach(subSummary->{
//            subSummary.getContractTxCountMap().forEach((contract,subTtc)->{
//                TokenTxCount sumTtc = contractTxCountMap.get(contract);
//                if(sumTtc==null) sumTtc = new TokenTxCount();
//                Map<String,Long> sumTokenTxCountMap = sumTtc.getTokenTxCountMap();
//                subTtc.getTokenTxCountMap().forEach((address,subCount)->{
//                    Long sumCount = sumTokenTxCountMap.get(address);
//                    if(sumCount==null) sumCount = 0L;
//                    sumCount +=subCount;
//                    sumTokenTxCountMap.put(address,sumCount);
//                });
//                contractTxCountMap.put(contract,sumTtc);
//            });
//        });
//
//        Arrays.asList(equalSummary,fromSummary).forEach(subSummary->{
//            subSummary.getContractTxCountMap().forEach((contract,subTtc)->{
//                TokenTxCount sumTtc = contractTxCountMap.get(contract);
//                if(sumTtc==null) sumTtc = new TokenTxCount();
//                sumTtc.setTokenTxCount(sumTtc.getTokenTxCount()+subTtc.getTokenTxCount());
//                contractTxCountMap.put(contract,sumTtc);
//            });
//        });
//
//        log.debug("summary\n{}", JSON.toJSONString(summary,true));
//
//        return summary;
//    }
//
//    public TokenTxSummary groupContractTxCount(Arc20TxGroupTypeEnum groupType,String scriptCondition){
//        TokenTxSummary tts = new TokenTxSummary();
//        try {
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//            if(StringUtils.isNotBlank(scriptCondition)){
//                searchSourceBuilder.from(0).size(0);
//                Map<String, Object> params = new HashMap<>();//存放参数的map
//                Script script =new Script(
//                        ScriptType.INLINE,
//                        "painless",
//                        scriptCondition,
//                        params
//                );
//                ScriptQueryBuilder scriptQueryBuilder = QueryBuilders.scriptQuery(script);
//                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//                boolQueryBuilder = boolQueryBuilder.must(scriptQueryBuilder);
//                searchSourceBuilder.query(boolQueryBuilder);
//            }
//
//
//            TermsAggregationBuilder inAggs = AggregationBuilders
//                    .terms(groupType.getTerms())
//                    .field(groupType.getField());
//            ValueCountAggregationBuilder inCountField = AggregationBuilders.count("tx_count").field(groupType.getField());
//            inAggs.subAggregation(inCountField);
//
//            TermsAggregationBuilder outAggs = AggregationBuilders
//                    .terms(Arc20TxGroupTypeEnum.CONTRACT.getTerms())
//                    .field(Arc20TxGroupTypeEnum.CONTRACT.getField());
//            ValueCountAggregationBuilder outCountField = AggregationBuilders.count("tx_count").field(Arc20TxGroupTypeEnum.CONTRACT.getField());
//            outAggs.subAggregation(outCountField);
//
//            outAggs.subAggregation(inAggs);
//
//            searchSourceBuilder.aggregation(outAggs);
//            SearchRequest searchRequest = new SearchRequest(config.getErc20TxIndexName()).source(searchSourceBuilder);
//            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
//            ParsedStringTerms contractTerms = response.getAggregations().get(Arc20TxGroupTypeEnum.CONTRACT.getTerms());
//            List<? extends Terms.Bucket> contractBuckets = contractTerms.getBuckets();
//            contractBuckets.forEach(tokenGroup -> {
//                TokenTxCount ttc = new TokenTxCount();
//                String tokenAddress = (String) tokenGroup.getKey();
//                ParsedValueCount tokenValueCount = tokenGroup.getAggregations().get("tx_count");
//                double tokenTxCount = tokenValueCount.value();
//                ttc.setTokenTxCount((long)tokenTxCount);
//                tts.getContractTxCountMap().put(tokenAddress,ttc);
//
//                ParsedStringTerms innerTerms = tokenGroup.getAggregations().get(groupType.getTerms());
//                innerTerms.getBuckets().forEach(innerGroup->{
//                    String address = (String) innerGroup.getKey();
//                    ParsedValueCount valueCount = innerGroup.getAggregations().get("tx_count");
//                    double txCount = valueCount.value();
//                    ttc.getTokenTxCountMap().put(address,(long)txCount);
//                    log.info("name , count {} {}", address, txCount);
//                });
//            });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        tts.calculate();
//        return tts;
//    }*/
//}
