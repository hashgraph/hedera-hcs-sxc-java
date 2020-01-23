package com.hedera.hcsapp;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Log4j2
public class DataSourceCustomizer {
    @Bean(name = "dataSourceOverride")
    @Primary
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource masterDataSource() {
        DataSource ds = null;
        try {
            AppData appData = new AppData();
             ds = DataSourceBuilder.create()
                    .url("jdbc:h2:./h2data/demo-db-appid-"+appData.getAppId()+"-"+appData.getUserName())
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(DataSourceCustomizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ds;
    }
 
}