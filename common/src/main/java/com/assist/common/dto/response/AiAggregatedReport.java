package com.assist.common.dto.response;

import com.assist.common.entity.*;
import lombok.Data;
import java.util.List;

/**
 * AI聚合报告响应
 */
@Data
public class AiAggregatedReport {
    private Patient patient;
    private Session session;
    private List<AiSymptomStructured> symptoms;
    private List<AiPreDiagnosis> diagnoses;
    private AiRiskAssessment riskAssessment;
    private List<AiTestSuggestion> testSuggestions;
    private AiSessionSummary sessionSummary;
    private List<AiPrescription> prescriptions;
}

