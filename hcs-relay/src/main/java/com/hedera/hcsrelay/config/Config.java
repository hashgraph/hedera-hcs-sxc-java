package com.hedera.hcsrelay.config;

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
    
    public Config() throws Exception {
        Yaml yaml = new Yaml(new Constructor(YAMLConfig.class));
        
        InputStream inputStream;
        File configFile = new File("./config.yaml");
        if (configFile.exists()) {
            // config file exists outside of jar, use it
            try {
                inputStream = new FileInputStream(configFile.getCanonicalPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new Exception ("Unable to locate ./config.yaml file");
            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception ("Error reading ./config.yaml file");
            }
        } else {
            inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("config.yaml");

        }
        yamlConfig = yaml.load(inputStream);
    }
    public YAMLConfig getConfig() {
        return this.yamlConfig;
    }
}
