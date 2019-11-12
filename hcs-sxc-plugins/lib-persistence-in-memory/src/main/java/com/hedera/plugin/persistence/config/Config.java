package com.hedera.plugin.persistence.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Manages configuration
 */

public final class Config {
    private YAMLConfig yamlConfig = new YAMLConfig();
    
    public Config() throws FileNotFoundException, IOException {
        Yaml yaml = new Yaml(new Constructor(YAMLConfig.class));
        
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("config.yaml");
        
        File configFile = new File("./config.yaml");
        if (configFile.exists()) {
            // config file exists outside of jar, use it
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        } 
        yamlConfig = yaml.load(inputStream);
    }
    public YAMLConfig getConfig() {
        return this.yamlConfig;
    }
}
