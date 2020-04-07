package com.hedera.tokendemo.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ServerPortCustomizer 
  implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Value("${app.port:8080}")
    private int port;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        try {
            factory.setPort(port);
        } catch (Exception e) {
            log.error(e);
        }
    }
}