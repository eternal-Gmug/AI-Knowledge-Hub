package com.scut.industrial_software.aiknowledgehub.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在 Spring 容器启动前读取项目根目录的 {@code .env} 文件，
 * 将其中的键值写入系统属性，使 {@code application.yml} 里的
 * {@code ${KEY:default}} 占位符能解析到 {@code .env} 中的值。
 *
 * <p>优先级：真实环境变量 &gt; JVM 系统属性 &gt; {@code .env}。</p>
 */
public final class DotenvConfig {

    private static final Logger log = LoggerFactory.getLogger(DotenvConfig.class);

    private DotenvConfig() {
    }

    /**
     * 加载 .env 到系统属性（不覆盖已存在的环境变量 / 系统属性）。
     */
    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();

        int applied = 0;
        for (var entry : dotenv.entries()) {
            String key = entry.getKey();
            if (System.getenv(key) != null || System.getProperty(key) != null) {
                continue;
            }
            System.setProperty(key, entry.getValue());
            applied++;
        }

        if (applied > 0) {
            log.info("Loaded {} key(s) from .env", applied);
        }
    }
}
