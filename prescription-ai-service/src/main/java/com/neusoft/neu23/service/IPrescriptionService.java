package com.neusoft.neu23.service;

import com.assist.common.dto.request.PrescriptionGenerateRequest;
import com.assist.common.dto.response.PrescriptionGenerateResponse;
import com.assist.common.entity.AiPrescription;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 处方生成服务接口
 * 继承 MyBatis-Plus 的 IService，既提供业务方法，又具备 AiPrescription 的通用 CRUD 能力。
 */
public interface IPrescriptionService extends IService<AiPrescription> {

    /**
     * 生成处方建议
     *
     * @param request 处方生成请求
     * @return 处方生成响应
     */
    PrescriptionGenerateResponse generatePrescription(PrescriptionGenerateRequest request);

    /**
     * 根据患者ID和会话ID查询处方建议
     *
     * @param patientId 患者ID
     * @param sessionId 会话ID
     * @return 处方生成响应
     */
    PrescriptionGenerateResponse getPrescriptionBySession(Integer patientId, Integer sessionId);
}

