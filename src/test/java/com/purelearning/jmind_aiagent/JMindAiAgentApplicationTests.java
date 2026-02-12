package com.purelearning.jmind_aiagent;

import com.purelearning.jmind_aiagent.agent.demo.JChatMindV3;
import com.purelearning.jmind_aiagent.config.ChatClientRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class JMindAiAgentApplicationTests {

    @Autowired
    private ChatClientRegistry chatClientRegistry;

    @Test
    void testAgentWithDeepSeek() {
        // 1. 从注册表中获取对应的 ChatClient
        ChatClient deepSeekChatClient = chatClientRegistry.get("deepseek-chat");
        assertNotNull(deepSeekChatClient, "未能获取到 deepseek-chat 客户端，请检查配置");

        // 2. 初始化你的 JChatMindV3 代理
        // 模拟从数据库或配置中读取 Agent 信息
        JChatMindV3 psychologistAgent = new JChatMindV3(
                "心语导师",
                "专业心理咨询 AI",
                "你是一个资深的心理学家，擅长用同理心倾听并给出建议。请保持语气温和。",
                deepSeekChatClient,
                10,
                "session-test-001"
        );

        log.info("--- 开始集成测试：{} ---", psychologistAgent);

        // 3. 第一轮对话：抛出问题
        String userInput1 = "你好，我最近感觉工作压力很大，很焦虑。";
        log.info("用户：{}", userInput1);

        String response1 = psychologistAgent.chat(userInput1);
        log.info("AI（心理学家）：{}", response1);

        assertNotNull(response1);

        // 4. 第二轮对话：测试记忆（上下文）
        // 如果记忆功能正常，AI 应该知道“这种焦虑”指代的是刚才提到的“工作压力”
        String userInput2 = "你觉得我该如何缓解这种焦虑呢？";
        log.info("用户：{}", userInput2);

        String response2 = psychologistAgent.chat(userInput2);
        log.info("AI（心理学家）：{}", response2);

        // 5. 验证记忆是否生效
        log.info("--- 对话历史统计 ---");
        log.info("当前消息总数：{}", psychologistAgent.getConversationHistory().size());

        // 期望：System(1) + User(2) + Assistant(2) = 5条消息
        assertTrue(psychologistAgent.getConversationHistory().size() >= 3);
    }
}