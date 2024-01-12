package com.turn.browser.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.elasticsearch.high-level-client")
public class EsClusterConfig {

    /**
     * Cluster address, multiple separated by
     */
    private List<String> hosts;

    /**
     *Port number used
     */
    private int port;

    /**
     *Protocol used
     */
    private String schema;

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;

    /**
     * Connection timeout period
     */
    private int connectTimeOut = 10000;

    /**
     * Connection timeout period
     */
    private int socketTimeOut = 30000;

    /**
     * Get the connection timeout
     */
    private int connectionRequestTimeOut = 5000;

    /**
     *Maximum number of connections
     */
    private int maxConnectNum = 200;

    /**
     * Maximum number of routing connections
     */
    private int maxConnectPerRoute = 200;

    @Bean(name = "restHighLevelClient")
    public RestHighLevelClient client() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        List<HttpHost> hostList = new ArrayList<>();
        hosts.forEach(host -> hostList.add(HttpHost.create(host)));
        RestClientBuilder builder = RestClient.builder(hostList.toArray(new HttpHost[0]));
        // Asynchronous httpclient connection delay configuration
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(connectTimeOut);
            requestConfigBuilder.setSocketTimeout(socketTimeOut);
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
            return requestConfigBuilder;
        });
        // Asynchronous httpclient connection number configuration
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(maxConnectNum);
            httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpClientBuilder;
        });
        return new RestHighLevelClient(builder);
    }

}