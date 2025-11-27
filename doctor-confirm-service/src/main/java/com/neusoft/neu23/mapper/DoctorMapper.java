package com.neusoft.neu23.mapper;

import com.assist.common.entity.Doctor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 医生Mapper接口
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    
    /**
     * 根据姓名和电话查询医生（登录验证）
     */
    @Select("SELECT * FROM doctor WHERE name = #{name} AND phone = #{phone}")
    Doctor selectByNameAndPhone(String name, String phone);
    
    /**
     * 根据医生ID查询该医生的患者列表（通过appointment表关联）
     */
    @Select("SELECT DISTINCT p.* FROM patient p " +
            "INNER JOIN appointment a ON p.id = a.patient_id " +
            "WHERE a.doctor_id = #{doctorId}")
    List<com.assist.common.entity.Patient> selectPatientsByDoctorId(Integer doctorId);
}




