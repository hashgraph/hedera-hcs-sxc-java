package com.hedera.hcsapp.dockercomposereader;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

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

    public static DockerCompose parse(String dockerFileLocation) throws Exception {
        InputStream inputStream = null;

        log.info("Loading app net configuration from docker-compose.yml");

        File configFile = new File(dockerFileLocation);
        if (configFile.exists()) {
            log.info("Found app net configuration in " + dockerFileLocation);
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        }
        if (inputStream != null) {
            Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            Yaml yaml = new Yaml(new Constructor(DockerCompose.class),representer);
            return yaml.load(inputStream);
        } else {
            throw new Exception("Docker compose file not found in " + dockerFileLocation);
        }
    }

    public static DockerCompose parse() throws Exception {
        return parse("./config/docker-compose.yml");
    }
}
