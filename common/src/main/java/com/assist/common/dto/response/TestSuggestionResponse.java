package com.assist.common.dto.response;

import com.assist.common.entity.AiTestSuggestion;
import lombok.Data;
import java.util.List;

/**
 * 检查建议响应
 */
@Data
public class TestSuggestionResponse {
    private List<AiTestSuggestion> testSuggestions;
    private String message;
}

