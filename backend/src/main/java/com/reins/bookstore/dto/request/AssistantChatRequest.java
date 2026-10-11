package com.reins.bookstore.dto.request;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AssistantChatRequest {
    private List<Map<String, Object>> messages = new ArrayList<>();
}
