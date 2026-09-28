package com.reins.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistantChatResponse {
    private String reply;
    private List<ToolCallLog> toolCalls;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCallLog {
        private String name;
        private Map<String, Object> arguments;
        private Object result;
        private String status;
    }
}
