package com.assist.common.dto.response;

import com.assist.common.entity.AiPrescription;
import lombok.Data;
import java.util.List;

/**
 * 处方生成响应
 */
@Data
public class PrescriptionGenerateResponse {
    private List<AiPrescription> prescriptions;
    private String message;
}

