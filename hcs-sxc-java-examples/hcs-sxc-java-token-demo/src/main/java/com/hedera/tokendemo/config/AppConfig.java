package com.hedera.tokendemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class AppConfig {
    @Value("${app.user}")
    public String demoUser;
    @Value("${hedera.app.configFile}")
    public String configFile; // = "./config/config.yaml";
    @Value("${hedera.app.environmentFilePath}")
    public String environmentFilePath; // = "./config/";
    @Value("${app.port:8080}")
    public int port;
}
