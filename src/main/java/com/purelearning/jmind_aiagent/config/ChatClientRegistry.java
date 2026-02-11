package com.purelearning.jmind_aiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 设置ChatClientRegistry，可以快速得到对应名称的大模型ChatClient对象
 */
@Component
public class ChatClientRegistry {

    private final Map<String, ChatClient> chatClients;

    public ChatClientRegistry(Map<String, ChatClient> chatClients) {
        this.chatClients = chatClients;
    }

    public ChatClient get(String key) {
        ChatClient client = chatClients.get(key);
        if (client == null) {
            throw new IllegalArgumentException("未找到名为 [" + key + "] 的 AI 模型，请检查配置！");
        }
        return client;
    }

}
