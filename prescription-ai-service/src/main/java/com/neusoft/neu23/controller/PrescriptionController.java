package com.neusoft.neu23.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.PrescriptionGenerateRequest;
import com.assist.common.dto.response.PrescriptionGenerateResponse;
import com.neusoft.neu23.service.IPrescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 处方生成Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/prescription")
public class PrescriptionController {

    @Autowired
    private IPrescriptionService prescriptionService;

    /**
     * 生成处方建议
     * 
     * @param request 处方生成请求
     * @return 处方生成响应
     */
    @PostMapping("/generate")
    public ApiResponse<PrescriptionGenerateResponse> generatePrescription(
            @RequestBody PrescriptionGenerateRequest request) {

        log.info("收到处方生成请求: patientId={}, sessionId={}",
                request.getPatientId(), request.getSessionId());
        try {
            // 参数验证
            if (request.getPatientId() == null || request.getSessionId() == null) {
                return ApiResponse.error(ErrorCode.PARAM_ERROR.ordinal(), "患者ID和会话ID不能为空");
            }
            // 生成处方
            PrescriptionGenerateResponse response = prescriptionService.generatePrescription(request);
            
            log.info("处方生成成功: patientId={}, sessionId={}, 处方数量={}", 
                    request.getPatientId(), request.getSessionId(), 
                    response.getPrescriptions() != null ? response.getPrescriptions().size() : 0);

            return ApiResponse.success(response);

        } catch (BusinessException e) {
            log.error("处方生成业务异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getErrorCode().ordinal(), e.getMessage());
        } catch (Exception e) {
            log.error("处方生成系统异常", e);
            return ApiResponse.error(ErrorCode.SERVICE_ERROR.ordinal(), "处方生成失败: " + e.getMessage());
        }
    }

    /**
     * 查询处方建议
     * 
     * @param patientId 患者ID
     * @param sessionId 会话ID
     * @return 处方生成响应
     */
    @GetMapping("/query")
    public ApiResponse<PrescriptionGenerateResponse> getPrescription(
            @RequestParam Integer patientId,
            @RequestParam Integer sessionId) {
        log.info("查询处方建议: patientId={}, sessionId={}", patientId, sessionId);

        try {
            // 参数验证
            if (patientId == null || sessionId == null) {
                return ApiResponse.error(ErrorCode.PARAM_ERROR.ordinal(), "患者ID和会话ID不能为空");
            }

            // 查询处方
            PrescriptionGenerateResponse response = prescriptionService.getPrescriptionBySession(patientId, sessionId);
            
            log.info("处方查询成功: patientId={}, sessionId={}, 处方数量={}", 
                    patientId, sessionId, 
                    response.getPrescriptions() != null ? response.getPrescriptions().size() : 0);

            return ApiResponse.success(response);

        } catch (BusinessException e) {
            log.error("处方查询业务异常: {}", e.getMessage(), e);
            return ApiResponse.error(e.getErrorCode().ordinal(), e.getMessage());
        } catch (Exception e) {
            log.error("处方查询系统异常", e);
            return ApiResponse.error(ErrorCode.SERVICE_ERROR.ordinal(), "处方查询失败: " + e.getMessage());
        }
    }
}

