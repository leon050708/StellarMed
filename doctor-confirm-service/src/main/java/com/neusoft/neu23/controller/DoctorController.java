package com.neusoft.neu23.controller;

import com.assist.common.common.ApiResponse;
import com.assist.common.entity.Doctor;
import com.assist.common.entity.Patient;
import com.neusoft.neu23.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生控制器
 * 提供医生登录和查询患者功能
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {
    
    private final DoctorService doctorService;
    
    /**
     * 医生登录
     * POST /api/doctor/login
     * 参数：name（姓名）, phone（电话）
     */
    @PostMapping("/login")
    public ApiResponse<Doctor> login(@RequestParam String name, 
                                     @RequestParam String phone) {
        log.info("医生登录请求: name={}, phone={}", name, phone);
        Doctor doctor = doctorService.login(name, phone);
        return ApiResponse.success(doctor);
    }
    
    /**
     * 查询该医生的患者列表
     * GET /api/doctor/patients?doctorId=xxx
     */
    @GetMapping("/patients")
    public ApiResponse<List<Patient>> getPatients(@RequestParam Integer doctorId) {
        log.info("查询医生患者列表: doctorId={}", doctorId);
        List<Patient> patients = doctorService.getPatientsByDoctorId(doctorId);
        return ApiResponse.success(patients);
    }
}




