package com.assist.common.dto.response;

import com.assist.common.entity.*;
import lombok.Data;
import java.util.List;

/**
 * 助诊报告总览响应
 */
@Data
public class AssistReportOverview {
    private Patient basicInfo;
    private List<AiSymptomStructured> structuredSymptoms;
    private List<AiPreDiagnosis> diagnoses;
    private AiRiskAssessment risk;
    private List<AiTestSuggestion> testSuggestions;
    private AiSessionSummary summary;
    private List<AiPrescription> aiPrescription;
}

