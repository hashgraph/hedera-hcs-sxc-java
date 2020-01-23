package com.hedera.hcs.sxc.plugin.mirror.config;

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
    private YAMLConfig yamlConfig = new YAMLConfig();
    
    public Config() throws FileNotFoundException, IOException {
        Yaml yaml = new Yaml(new Constructor(YAMLConfig.class));
        
        InputStream inputStream;
        
        File configFile = new File("./config/queue-config.yaml");
        if (configFile.exists()) {
            log.info("Loading queue-config.yaml from ./config");
            // config file exists outside of jar, use it
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        } else {
            configFile = new File("./queue-config.yaml");
            if (configFile.exists()) {
                log.info("Loading queue-config.yaml from ./");
                // config file exists outside of jar, use it
                inputStream = new FileInputStream(configFile.getCanonicalPath());
            } else {
                inputStream = this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("queue-config.yaml");
                log.info("Loading queue-config.yaml from src/main/resources");
            }
        }
        yamlConfig = yaml.load(inputStream);
    }
    public YAMLConfig getConfig() {
        return this.yamlConfig;
    }
}
