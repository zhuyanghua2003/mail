package com.msb.mall.search.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MallElasticSearchConfiguration {

    private static final String ES_USERNAME = "elastic";
    // ES 密码（安装 ES 时设置的密码，或初始化时的默认密码）
    private static final String ES_PASSWORD = "123456a";
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY, // 对所有地址和端口都使用此凭证
                new UsernamePasswordCredentials(ES_USERNAME, ES_PASSWORD)
        );
        // 2. 构建 RestClientBuilder
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("192.168.6.128", 9200, "http")
        );

        // 3. 配置 HTTP 客户端，添加认证信息
        builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        );

        // 4. 创建并返回 RestHighLevelClient 实例
        return new RestHighLevelClient(builder);
    }

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }
}
