import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.mall.search.MallSearchStarterApp;
import com.msb.mall.search.config.MallElasticSearchConfiguration;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@SpringBootTest(classes = MallSearchStarterApp.class)
class MallProducetApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    @Test
    void contextLoads() {
        System.out.println("---->"+ client);
    }
    @Test
    void searchIndexAll() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("sms_submit_log_2023"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /*sourceBuilder.query();
        sourceBuilder.from();
        sourceBuilder.size();
        sourceBuilder.aggregation();*/
        searchRequest.source(sourceBuilder);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println("ElasticSearch检索的信息："+response);
    }

    @Test
    void searchIndexByAddress() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("sms_submit_log_2023"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询出bank下 address 中包含 mill的记录
        sourceBuilder.query(QueryBuilders.matchQuery("ip","192.168.6.128"));
        searchRequest.source(sourceBuilder);
        // System.out.println(searchRequest);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println("ElasticSearch检索的信息："+response);
    }
    //测试保存文档
    @Test
    void saveIndex() throws IOException {
        IndexRequest indexRequest=new IndexRequest("system");
        indexRequest.id("1");
        //indexRequest.source("name","张三","age",18,"gender","男");
        User user=new User("张三",18,"男");
        ObjectMapper objectMapper=new ObjectMapper();
        String json=objectMapper.writeValueAsString(user);
        indexRequest.source(json, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        System.out.println( index);

    }
    class User{
        private String name;
        private Integer age;
        private String gender;
        public User(String name, Integer age, String gender) {
            this.name = name;
            this.age = age;
            this.gender = gender;
        }
        // 添加 Getter 方法
        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

        public String getGender() {
            return gender;
        }
    }
    @Test
    void searchIndexAggregation() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("sms_submit_log_2023"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询出bank下 所有的文档
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        // 聚合 aggregation
        // 聚合bank下年龄的分布和每个年龄段的平均薪资
        AggregationBuilder aggregationBuiler = AggregationBuilders.terms("ageAgg")
                .field("age")
                .size(10);
        // 嵌套聚合
        aggregationBuiler.subAggregation(AggregationBuilders.avg("balanceAvg").field("balance"));

        sourceBuilder.aggregation(aggregationBuiler);
        sourceBuilder.size(0); // 聚合的时候就不用显示满足条件的文档内容了
        searchRequest.source(sourceBuilder);
        System.out.println(sourceBuilder);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println(response);
    }



}
