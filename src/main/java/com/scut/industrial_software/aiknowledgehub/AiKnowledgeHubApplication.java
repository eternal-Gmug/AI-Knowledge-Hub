package com.scut.industrial_software.aiknowledgehub;

import com.scut.industrial_software.aiknowledgehub.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiKnowledgeHubApplication {

    public static void main(String[] args) {
        DotenvConfig.load();
        SpringApplication.run(AiKnowledgeHubApplication.class, args);
    }

}
