package com.hedera.hcsapp.dockercomposereader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import lombok.extern.log4j.Log4j2;

/**
 * Manages configuration
 */

@Log4j2
public final class DockerComposeReader {
    
    public static DockerCompose parse() throws Exception {
        InputStream inputStream = null;
        
        log.info("Loading app net configuration from docker-compose.yml");
        
        File configFile = new File("./config/docker-compose.yml");
        if (configFile.exists()) {
            log.info("Found app net configuration in ./config/docker-compose.yml");
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        }
        if (inputStream != null) {
            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(new Constructor(DockerCompose.class),representer);
            return yaml.load(inputStream);
        } else {
            throw new Exception("Docker compose file not found in ./config");
        }
    }
}
