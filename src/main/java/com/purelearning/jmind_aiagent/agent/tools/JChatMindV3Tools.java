package com.purelearning.jmind_aiagent.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class JChatMindV3Tools {

    @Tool(description = "获取当前的系统日期和时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "根据城市名称查询天气")
    public String getWeather(String city) {
        return city + "今天天气晴朗，25°C";
    }

}
