package com.hedera.hcs.sxc.relay.config;


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

    public Config() throws Exception {
        Yaml yaml = new Yaml(new Constructor(YAMLConfig.class));

        InputStream inputStream;
        File configFile = new File("./relay-config.yaml");
        if (configFile.exists()) {
            // config file exists outside of jar, use it
            try {
                log.info("Loading config from ./relay-config.yaml");
                inputStream = new FileInputStream(configFile.getCanonicalPath());
            } catch (FileNotFoundException e) {
                log.error(e);
                throw new Exception ("Unable to locate ./relay-config.yaml file");
            } catch (IOException e) {
                log.error(e);
                throw new Exception ("Error reading ./relay-config.yaml file");
            }
        } else {
            log.info("Loading config from ./src/main/resources/relay-config.yaml");
            inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("relay-config.yaml");

        }
        yamlConfig = yaml.load(inputStream);
    }
    public YAMLConfig getConfig() {
        return this.yamlConfig;
    }
}
