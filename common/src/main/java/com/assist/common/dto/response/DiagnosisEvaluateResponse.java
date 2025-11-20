package com.assist.common.dto.response;

import com.assist.common.entity.AiPreDiagnosis;
import lombok.Data;
import java.util.List;

/**
 * 诊断评估响应
 */
@Data
public class DiagnosisEvaluateResponse {
    private List<AiPreDiagnosis> diagnoses;
    private String message;
}

