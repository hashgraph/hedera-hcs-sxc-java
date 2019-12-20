package com.hedera.hcsapp.dockercomposereader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Manages configuration
 */

public final class DockerComposeReader {
    
    public static DockerCompose parse() throws Exception {
        InputStream inputStream;
        File configFile = new File("./docker-compose.yml");
        if (configFile.exists()) {
            inputStream = new FileInputStream(configFile.getCanonicalPath());
            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(new Constructor(DockerCompose.class),representer);
            return yaml.load(inputStream);
        } else {
            throw new Exception("Docker compose file not found.");
        }
    }
}
