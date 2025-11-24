package com.assist.symptom.controller;

import com.assist.common.dto.request.SymptomExtractRequest;
import com.assist.symptom.service.SymptomExtractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 症状AI控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class SymptomAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testExtractSymptoms() throws Exception {
        SymptomExtractRequest request = new SymptomExtractRequest();
        request.setPatientId(1);
        request.setSessionId(1001);

        mockMvc.perform(post("/api/ai/symptoms/extract")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}

