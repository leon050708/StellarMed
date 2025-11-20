package com.assist.common.dto.response;

import com.assist.common.entity.AiSymptomStructured;
import lombok.Data;
import java.util.List;

/**
 * 症状提取响应
 */
@Data
public class SymptomExtractResponse {
    private List<AiSymptomStructured> structuredSymptoms;
    private String message;
}

