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

import java.util.Map;

public final class DockerCompose {
    private String version;
    private Map<String, DockerService> services;
    
    public String getVersion() {
        return this.version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public Map<String, DockerService> getServices() {
        return this.services;
    }
    public void setServices(Map<String, DockerService> services) {
        this.services = services;
    }
    public String getNameForId(String appId) {
        String name = "not found";
        for (Map.Entry<String, DockerService> service : this.services.entrySet()) {
            DockerService dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().containsKey("APP_ID")) {
                    if (dockerService.getEnvironment().get("APP_ID").contentEquals(appId)) {
                        return dockerService.getContainer_name();
                    }
                }
            }
        }
        return name;
    }
    public String getPublicKeyForId(String appId) {
        String key = "not found";
        for (Map.Entry<String, DockerService> service : this.services.entrySet()) {
            DockerService dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().containsKey("PUBKEY")) {
                    if (dockerService.getEnvironment().get("APP_ID").contentEquals(appId)) {
                        return dockerService.getEnvironment().get("PUBKEY");
                    }
                }
            }
        }
        return key;
    }
    public int getPortForId(String appId) {
        int port = 0;
        for (Map.Entry<String, DockerService> service : this.services.entrySet()) {
            DockerService dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().get("APP_ID").contentEquals(appId)) {
                    port = dockerService.getPortAsInteger();
                }
            }
        }
        return port;
    }
}
