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
        this("./config/apps.yaml");
    }
    
    // constructor with parameter for testing
    public Config(String configFilePath) throws FileNotFoundException, IOException {
        Yaml yaml = new Yaml(new Constructor(AppYAML.class));
        
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            log.info("Loading apps.yaml from " + configFilePath);
            // config file exists outside of jar, use it
            InputStream inputStream = new FileInputStream(configFile.getCanonicalPath());
            yamlConfig = yaml.load(inputStream);
        } else {
            log.error("Unable to find file " + configFilePath);
        }
    }

    public AppYAML getConfig() {
        return this.yamlConfig;
    }
}
