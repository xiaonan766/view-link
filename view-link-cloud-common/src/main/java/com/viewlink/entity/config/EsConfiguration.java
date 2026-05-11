package com.viewlink.entity.config;


import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import javax.annotation.Resource;

@Configuration
public class EsConfiguration extends AbstractElasticsearchConfiguration implements DisposableBean {
    @Resource
    private AppConfig appConfig;

    private RestHighLevelClient client;

    @Override
    public void destroy() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public RestHighLevelClient elasticsearchClient() {
        //ClientConfiguration是Elasticsearch客户端的配置类，通过ClientConfiguration.builder()创建一个配置构建器，
        //调用connectedTo方法并传入Elasticsearch的主机和端口信息，最后调用 build方法构建配置实例。
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(appConfig.getEsHostPort()).build();
        client = RestClients.create(clientConfiguration).rest();
        return client;
    }
}
