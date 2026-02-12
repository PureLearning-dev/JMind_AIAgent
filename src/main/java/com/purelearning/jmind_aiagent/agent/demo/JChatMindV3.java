package com.purelearning.jmind_aiagent.agent.demo;

import com.purelearning.jmind_aiagent.agent.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 实现一个最小化Agent Demo
 * 目的：使用这个Demo完成理解Agent如何调用大模型、对话上下文如何被维护的、系统提示词在什么时候生效、大模型记忆功能如何实现的
 */
@Slf4j
public class JChatMindV3 {
    protected String name;
    protected String description;
    protected String systemPrompt;
    protected ChatClient chatClient;
    protected ChatMemory chatMemory;
    protected AgentState agentState;
    protected String sessionId;

    private static final Integer DEFAULT_MAX_MESSAGES = 20;

    public JChatMindV3(){

    }

    public JChatMindV3(String name,
                       String description,
                       String systemPrompt,
                       ChatClient chatClient,
                       Integer maxMessages,
                       String sessionId) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.chatClient = chatClient;
        this.sessionId = sessionId != null ? sessionId : "default-session";
        this.agentState = AgentState.IDLE;

        // 初始化聊天记忆
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(maxMessages != null ? maxMessages : DEFAULT_MAX_MESSAGES)
                .build();

        // 添加系统提示词
        if (StringUtils.hasLength(systemPrompt)) {
            this.chatMemory.add(this.sessionId, new SystemMessage(systemPrompt));
        }
    }

    /**
     * 处理用户输入并返回 AI 回复
     */
    public String chat(String userInput) {
        Assert.notNull(userInput, "用户输入不能为空");

        // 要进行聊天，前提是Agent在空闲中
        if (agentState != AgentState.IDLE) {
            throw new IllegalStateException("Agent 状态不是 IDLE，当前状态：" + agentState);
        }

        try {

            // Agent进入思考状态
            agentState = AgentState.THINKING;

            // 添加用户消息到记忆
            UserMessage userMessage = new UserMessage(userInput);
            chatMemory.add(sessionId, userMessage);

            // 构建提示词
            Prompt prompt = Prompt.builder()
                    .messages(chatMemory.get(sessionId))
                    .build();

            // 调用 LLM
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

            Assert.notNull(response, "ChatResponse 不能为空");

            AssistantMessage assistantMessage = response.getResult().getOutput();
            String aiResponse = assistantMessage.getText();

            // 将 AI 回复添加到记忆
            chatMemory.add(sessionId, assistantMessage);

            // Agent一次交互完成
            agentState = AgentState.FINISHED;

            return aiResponse;

        } catch (Exception e) {
            agentState = AgentState.ERROR;
            log.error("聊天过程中发生错误", e);
            throw new RuntimeException("聊天过程中发生错误", e);
        } finally {
            // 重置状态以便下次使用
            agentState = AgentState.IDLE;
        }
    }

    /**
     * 获取当前对话历史
     */
    public List<Message> getConversationHistory() {
        return chatMemory.get(sessionId);
    }

    /**
     * 重置对话历史
     */
    public void reset() {
        chatMemory.clear(sessionId);
        if (StringUtils.hasLength(systemPrompt)) {
            chatMemory.add(sessionId, new SystemMessage(systemPrompt));
        }
        agentState = AgentState.IDLE;
    }

    @Override
    public String toString() {
        return "JChatMindV1 {" +
                "name = " + name + ",\n" +
                "description = " + description + ",\n" +
                "systemPrompt = " + systemPrompt + "}";
    }

}
