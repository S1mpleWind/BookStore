package com.reins.bookstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reins.bookstore.dto.request.AssistantChatRequest;
import com.reins.bookstore.dto.response.AssistantChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class AssistantService {
    private static final int MAX_TOOL_ROUNDS = 3;
    private final ObjectMapper mapper;
    private final RestClient client;
    private final AssistantSkillService skills;
    private final String apiKey;
    private final String model;

    public AssistantService(ObjectMapper mapper, AssistantSkillService skills,
                            @Value("${openai.api-key:}") String apiKey,
                            @Value("${openai.model:gpt-4o-mini}") String model,
                            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl) {
        this.mapper = mapper;
        this.skills = skills;
        this.apiKey = apiKey;
        this.model = model;
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    public AssistantChatResponse chat(AssistantChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("未配置 OPENAI_API_KEY，请先设置 OpenAI API Key");
        }
        ArrayNode messages = mapper.createArrayNode();
        if (request != null && request.getMessages() != null) {
            for (Map<String, Object> message : request.getMessages()) {
                ObjectNode node = mapper.createObjectNode();
                if (message != null) message.forEach((key, value) -> node.set(key, mapper.valueToTree(value)));
                if (node.has("role") && node.has("content")) messages.add(node);
            }
        }
        if (messages.isEmpty()) throw new IllegalArgumentException("messages 不能为空");

        List<AssistantChatResponse.ToolCallLog> logs = new ArrayList<>();
        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            JsonNode response = callModel(messages);
            JsonNode assistant = response.path("choices").path(0).path("message");
            JsonNode toolCalls = assistant.path("tool_calls");
            if (!toolCalls.isArray() || toolCalls.isEmpty()) {
                return new AssistantChatResponse(assistant.path("content").asText("抱歉，模型没有返回可用的回复。"), logs);
            }

            ObjectNode assistantMessage = assistant.deepCopy();
            messages.add(assistantMessage);
            for (JsonNode toolCall : toolCalls) {
                String id = toolCall.path("id").asText();
                String name = toolCall.path("function").path("name").asText();
                String rawArguments = toolCall.path("function").path("arguments").asText("{}");
                Map<String, Object> arguments;
                Object result;
                try {
                    arguments = mapper.readValue(rawArguments, LinkedHashMap.class);
                    System.out.printf("[Function Calling] name=%s, arguments=%s%n", name, rawArguments);
                    result = executeTool(name, arguments);
                } catch (Exception ex) {
                    arguments = new LinkedHashMap<>();
                    result = Map.of("error", ex.getMessage() == null ? "工具执行失败" : ex.getMessage());
                }
                logs.add(new AssistantChatResponse.ToolCallLog(name, arguments, result,
                        result instanceof Map && ((Map<?, ?>) result).containsKey("error") ? "failed" : "completed"));
                ObjectNode toolMessage = mapper.createObjectNode();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", id);
                toolMessage.put("content", serializeResult(result));
                messages.add(toolMessage);
            }
        }
        throw new IllegalStateException("工具调用轮次超过限制，请稍后重试");
    }

    private String serializeResult(Object result) {
        try {
            return mapper.writeValueAsString(result);
        } catch (Exception ex) {
            return "{\"error\":\"工具结果序列化失败\"}";
        }
    }

    private Object executeTool(String name, Map<String, Object> args) {
        Object isbnValue = args.get("isbn");
        if (!(isbnValue instanceof String isbn) || isbn.isBlank() || isbn.length() > 64) {
            throw new IllegalArgumentException("isbn 必须是 1-64 个字符的非空字符串");
        }
        return switch (name) {
            case "check_inventory" -> skills.checkInventory(isbn);
            case "get_competitor_price" -> skills.getCompetitorPrice(isbn);
            default -> throw new IllegalArgumentException("未知函数: " + name);
        };
    }

    private JsonNode callModel(ArrayNode messages) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("tools", tools());
        payload.put("tool_choice", "auto");
        return client.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey).body(payload).retrieve().body(JsonNode.class);
    }

    private List<Map<String, Object>> tools() {
        return List.of(functionTool("check_inventory", "查询本店指定 ISBN 的实时库存。"),
                functionTool("get_competitor_price", "查询模拟竞争对手的销售价格。"));
    }

    private Map<String, Object> functionTool(String name, String description) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", Map.of("isbn", Map.of("type", "string", "description", "图书 ISBN 编号")));
        parameters.put("required", List.of("isbn"));
        parameters.put("additionalProperties", false);
        return Map.of("type", "function", "function", Map.of("name", name, "description", description, "parameters", parameters));
    }
}
