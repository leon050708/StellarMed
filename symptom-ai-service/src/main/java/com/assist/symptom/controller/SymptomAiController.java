package com.assist.symptom.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.common.dto.response.SymptomExtractResponse;
import com.assist.symptom.service.SymptomExtractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 症状AI控制器
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class SymptomAiController {

    private final SymptomExtractService symptomExtractService;

    /**
     * AI抽取结构化症状
     */
    @PostMapping("/symptoms/extract")
    public ApiResponse<SymptomExtractResponse> extractSymptoms(@RequestBody SymptomExtractRequest request) {
        SymptomExtractResponse response = symptomExtractService.extractStructuredSymptoms(request);
        return ApiResponse.success(response);
    }
}

