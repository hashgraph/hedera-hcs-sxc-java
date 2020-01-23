package com.hedera.hcsapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import lombok.extern.log4j.Log4j2;

/**
 * Manages configuration
 */
@Log4j2
public final class Config {
    private AppYAML yamlConfig = new AppYAML();
    
    public Config() throws FileNotFoundException, IOException {
        Yaml yaml = new Yaml(new Constructor(AppYAML.class));
        
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("apps.yaml");
        
        File configFile = new File("./config/apps.yaml");
        if (configFile.exists()) {
            log.info("Loading apps.yaml from ./config");
            // config file exists outside of jar, use it
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        } else {

            configFile = new File("./apps.yaml");
            if (configFile.exists()) {
                log.info("Loading apps.yaml from ./");
                // config file exists outside of jar, use it
                inputStream = new FileInputStream(configFile.getCanonicalPath());
            } else {
                log.info("Loading apps.yaml from src/main/resources");
            }
        }

        yamlConfig = yaml.load(inputStream);
    }
    public AppYAML getConfig() {
        return this.yamlConfig;
    }
}
