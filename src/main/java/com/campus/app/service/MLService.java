package com.campus.app.service;

import com.campus.app.dto.BillExtractionResponse;
import com.campus.app.dto.BudgetPredictionRequest;
import com.campus.app.dto.BudgetPredictionResponse;
import com.campus.app.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;

@Service
public class MLService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MLService(RestClient.Builder builder,
                     ObjectMapper objectMapper,
                     @Value("${app.ml.base-url:http://127.0.0.1:8000}") String baseUrl) {
        this.objectMapper = objectMapper;

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(60)); // PDF parsing can take a few seconds

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
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

    /** Sends a bill PDF to the ML service and returns the extracted fields. */
    public BillExtractionResponse extractBill(byte[] pdfBytes) {
        // Fixed filename: the original name is kept by the app, and a fixed ASCII
        // name avoids multipart header-encoding surprises.
        ByteArrayResource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return "bill.pdf";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        try {
            return restClient
                    .post()
                    .uri("/extract-bill")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(BillExtractionResponse.class);
        } catch (RestClientResponseException e) {
            throw new ApiException(readDetail(e), HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (ResourceAccessException e) {
            throw new ApiException(
                    "Bill extraction service is not reachable. Start ml-services (uvicorn main:app --port 8000).",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    // FastAPI errors look like {"detail": "..."}
    private String readDetail(RestClientResponseException e) {
        try {
            JsonNode node = objectMapper.readTree(e.getResponseBodyAsString());
            JsonNode detail = node.get("detail");
            if (detail != null && detail.isTextual()) {
                return detail.asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "Could not read this bill (extraction service returned " + e.getStatusCode().value() + ").";
    }
}
