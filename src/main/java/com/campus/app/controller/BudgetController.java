package com.campus.app.controller;


import com.campus.app.dto.BudgetPredictionRequest;
import com.campus.app.dto.BudgetPredictionResponse;
import com.campus.app.service.MLService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {
    private final MLService mlService;

    public BudgetController(MLService mlService) {

        this.mlService = mlService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<BudgetPredictionResponse> recommendBudget(
            @RequestBody BudgetPredictionRequest request) {

        BudgetPredictionResponse response =
                mlService.predictBudget(request);

        return ResponseEntity.ok(response);
    }
}
