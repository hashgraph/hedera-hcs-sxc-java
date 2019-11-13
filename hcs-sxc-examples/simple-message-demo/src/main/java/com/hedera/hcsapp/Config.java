package com.hedera.hcsapp;

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
    private AppYAML yamlConfig = new AppYAML();
    
    public Config() throws FileNotFoundException, IOException {
        Yaml yaml = new Yaml(new Constructor(AppYAML.class));
        
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("apps.yaml");
        
        File configFile = new File("./apps.yaml");
        if (configFile.exists()) {
            // config file exists outside of jar, use it
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        } 
        yamlConfig = yaml.load(inputStream);
    }
    public AppYAML getConfig() {
        return this.yamlConfig;
    }
}
