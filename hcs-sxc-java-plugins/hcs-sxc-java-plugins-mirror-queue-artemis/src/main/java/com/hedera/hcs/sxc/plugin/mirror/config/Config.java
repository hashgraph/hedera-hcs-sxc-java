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
        this("./config/queue-config.yaml");
    }

    // Constructor for testing
    public Config(String configFilePath) throws FileNotFoundException, IOException {
        Yaml yaml = new Yaml(new Constructor(YAMLConfig.class));
        
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            log.info("Loading queue-config.yaml from " + configFilePath);
            InputStream inputStream = new FileInputStream(configFile.getCanonicalPath());
            yamlConfig = yaml.load(inputStream);
        } else {
            log.error("Unable to find file " + configFilePath);
        }
    }

    public YAMLConfig getConfig() {
        return this.yamlConfig;
    }
}
