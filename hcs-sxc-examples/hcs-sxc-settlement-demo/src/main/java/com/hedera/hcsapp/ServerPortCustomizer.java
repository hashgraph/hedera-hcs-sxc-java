package com.hedera.hcsapp;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ServerPortCustomizer 
  implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
  
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        try {
            AppData appData = new AppData();
            factory.setPort(appData.getWebPort());
        } catch (Exception e) {
            log.error(e);
        }
    }
}