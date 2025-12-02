package com.msb.mall.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.common.dto.es.SkuESModel;
import com.msb.mall.search.config.MallElasticSearchConfiguration;
import com.msb.mall.search.constant.ESConstant;
import com.msb.mall.search.service.ElasticSearchSaveService;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ElasticSearchSaveServiceImpl implements ElasticSearchSaveService {
    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel skuESModel : skuESModels) {
            IndexRequest indexRequest=new IndexRequest(ESConstant.PRODUCT_INDEX);
            indexRequest.id(skuESModel.getSkuId().toString());
            ObjectMapper mapper=new ObjectMapper();
            String json = mapper.writeValueAsString(skuESModel);
            indexRequest.source(json, XContentType.JSON);
            bulkRequest.add(indexRequest);

        }

        BulkResponse bulk = client.bulk(bulkRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();
        for (BulkItemResponse item : bulk.getItems()) {
            if (item.isFailed()) {
                System.out.println("失败：" + item.getFailureMessage());
            }
        }


        return !b;
    }
}
