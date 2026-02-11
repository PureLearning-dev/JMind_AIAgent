package com.purelearning.jmind_aiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class JMindAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(JMindAiAgentApplication.class, args);
    }

}
