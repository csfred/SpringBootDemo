package com.cs.student.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Spring configuration
 *
 * @author cs
 * @date 2020/2/17
 */
@Configuration
public class EsClientConfig {

    private RestHighLevelClient client;

    @Value("${spring.elasticsearch.cluster.name}")
    private String clusterName;

    @Value("${spring.elasticsearch.cluster.host}")
    private String clusterHost;

    @Value("${spring.elasticsearch.cluster.port}")
    private Integer clusterPort;

    @Value("${spring.elasticsearch.cluster.scheme}")
    private String clusterScheme;

    @Value("${spring.elasticsearch.cluster.index}")
    private String indexName;

    public  String getIndexName(){
        return indexName;
    }

    /**
     * RestHighLevelClient client.
     * 如果配置X-PACK ,则需要在此处配置用户信息
     *
     * @return the rest high level client
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestHighLevelClient localClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(clusterHost,
                    clusterPort, clusterScheme)));
        client = localClient;
        return localClient;
    }

    /**
     * 避免Client每次使用创建和释放
     */
    public RestHighLevelClient esTemplate() {
        if ( null == client) {
            client = restHighLevelClient();
            return client;
        }
        return client;
    }
}
