package com.campus.app.service;

import com.campus.app.dto.BudgetPredictionRequest;
import com.campus.app.dto.BudgetPredictionResponse;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Service
public class MLService {

    private final RestClient restClient;

    public MLService(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.restClient = builder
                .baseUrl("http://127.0.0.1:8000")
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    public BudgetPredictionResponse predictBudget(BudgetPredictionRequest request) {
        return restClient
                .post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(BudgetPredictionResponse.class);
    }
}