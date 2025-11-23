package com.neusoft.neu23.service.impl;

import com.assist.common.common.ApiResponse;
import com.assist.common.common.BusinessException;
import com.assist.common.common.ErrorCode;
import com.assist.common.dto.request.*;
import com.assist.common.dto.response.*;
import com.assist.common.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.neusoft.neu23.mapper.DoctorFinalDiagnosisMapper;
import com.neusoft.neu23.service.DoctorConfirmService;
import com.neusoft.neu23.tc.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 医生最终确认服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorConfirmServiceImpl implements DoctorConfirmService {

    private final SummaryAiClient summaryAiClient;
    private final PrescriptionAiClient prescriptionAiClient;
    private final DoctorFinalDiagnosisMapper diagnosisMapper;

    @Override
    public AiAggregatedReport aggregateAssistReport(Integer patientId, Integer sessionId) {
        if (patientId == null || sessionId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "患者ID和会话ID不能为空");
        }

        AiAggregatedReport report = new AiAggregatedReport();
        
        // 注意：patient 和 session 信息暂不获取，因为 patient-service 尚未提供根据ID查询的接口
        // 如果后续需要，可以在 patient-service 中添加 GET /api/patients/{patientId} 和 GET /api/sessions/{sessionId} 接口
        report.setPatient(null);
        report.setSession(null);
        
        // 调用服务 5（summary-ai-service）获取 2、3、4 的数据 + 总结
        // 这样可以避免重复调用 2、3、4 服务，提高效率并保证数据一致性
        com.assist.common.dto.response.SummaryWithDataResponse summaryWithData = callAndUnwrap(() -> 
            summaryAiClient.generateWithData(patientId, sessionId), "summary-ai-service获取数据汇总");
        
        if (summaryWithData != null) {
            report.setSymptoms(safeList(summaryWithData.getSymptoms()));
            report.setDiagnoses(safeList(summaryWithData.getDiagnoses()));
            report.setRiskAssessment(summaryWithData.getRiskAssessment());
            report.setTestSuggestions(safeList(summaryWithData.getTestSuggestions()));
            report.setSessionSummary(summaryWithData.getSessionSummary());
        }
        
        // 调用服务 6（prescription-ai-service）获取处方建议
        PrescriptionGenerateRequest prescriptionRequest = new PrescriptionGenerateRequest();
        prescriptionRequest.setPatientId(patientId);
        prescriptionRequest.setSessionId(sessionId);
        PrescriptionGenerateResponse prescriptionResponse = callAndUnwrap(() -> 
            prescriptionAiClient.generate(prescriptionRequest), "prescription-ai-service获取AI处方");
        report.setPrescriptions(safeList(prescriptionResponse != null ? prescriptionResponse.getPrescriptions() : null));
        
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Void> saveFinalDiagnosis(DoctorFinalConfirmRequest request) {
        if (request == null
                || request.getPatientId() == null
                || request.getSessionId() == null
                || request.getDoctorId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "医生确认请求参数不完整");
        }

        DoctorFinalDiagnosis entity = new DoctorFinalDiagnosis();
        entity.setPatientId(request.getPatientId());
        entity.setSessionId(request.getSessionId());
        entity.setDoctorId(request.getDoctorId());
        entity.setFinalDiagnosis(request.getFinalDiagnosis());
        entity.setFinalPrescription(request.getFinalPrescription());
        entity.setComment(request.getComment());
        entity.setCreatedTime(new Date());

        LambdaQueryWrapper<DoctorFinalDiagnosis> wrapper = Wrappers.lambdaQuery(DoctorFinalDiagnosis.class)
                .eq(DoctorFinalDiagnosis::getSessionId, request.getSessionId());
        DoctorFinalDiagnosis existing = diagnosisMapper.selectOne(wrapper);
        if (existing == null) {
            diagnosisMapper.insert(entity);
            log.info("医生最终确认结果已写入 sessionId={}", request.getSessionId());
        } else {
            entity.setId(existing.getId());
            diagnosisMapper.updateById(entity);
            log.info("医生最终确认结果已更新 sessionId={}", request.getSessionId());
        }
        return ApiResponse.success("医生最终确认保存成功");
    }

    /**
     * 通用调用封装，兼容空响应或错误码
     */
    private <T> T callAndUnwrap(ClientCaller<ApiResponse<T>> caller, String actionLabel) {
        try {
            ApiResponse<T> response = caller.invoke();
            if (response == null) {
                throw new BusinessException(ErrorCode.SERVICE_ERROR, actionLabel + "返回为空");
            }
            if (response.getCode() != 0) {
                throw new BusinessException(ErrorCode.SERVICE_ERROR,
                        actionLabel + "失败：" + Optional.ofNullable(response.getMsg()).orElse("未知错误"));
            }
            return response.getData();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("{} 失败", actionLabel, ex);
            throw new BusinessException(ErrorCode.SERVICE_ERROR, actionLabel + "异常：" + ex.getMessage());
        }
    }

    private <T> List<T> safeList(List<T> data) {
        return data == null ? List.of() : data;
    }

    @FunctionalInterface
    private interface ClientCaller<T> {
        T invoke();
    }
}

