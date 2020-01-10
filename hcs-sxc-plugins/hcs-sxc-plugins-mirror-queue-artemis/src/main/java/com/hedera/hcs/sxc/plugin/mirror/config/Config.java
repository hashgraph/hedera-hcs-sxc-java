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
        
        File configFile = new File("./queue-config.yaml");
        if (configFile.exists()) {
            // config file exists outside of jar, use it
            log.info("Loading config from ./queue-config.yaml");
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        } else {
            log.info("Loading config from ./src/main/resources/queue-config.yaml");
            inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("queue-config.yaml");
        }
        yamlConfig = yaml.load(inputStream);
    }
    public YAMLConfig getConfig() {
        return this.yamlConfig;
    }
}
