package com.metron.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticSearchService {

    @Value("${elasticsearch.url}")
    private String elasticSearchUrl;

    @Value("${elasticsearch.api-key}")
    private String apiKey;

    @Value("${elasticsearch.index}")
    private String index;

    private final RestTemplate restTemplate;

    public ElasticSearchService() {
        // Configure RestTemplate with timeouts
        this.restTemplate = new RestTemplate(clientHttpRequestFactory());
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // Set connection timeout to 5 seconds
        factory.setReadTimeout(5000); // Set read timeout to 5 seconds
        return factory;
    }

    public void sendAlert(String filePath) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("message", "New file detected: " + filePath);
        alert.put("timestamp", System.currentTimeMillis());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "ApiKey " + apiKey); // Set API Key here

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(alert, headers);
        String endpoint = elasticSearchUrl + "/" + index + "/_doc";

        try {
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Failed to push alert to ElasticSearch: " + response.getBody());
            } else {
                System.out.println("Alert successfully pushed to ElasticSearch.");
            }
        } catch (Exception e) {
            System.err.println("Error sending alert to Elasticsearch: " + e.getMessage());
        }
    }
}
