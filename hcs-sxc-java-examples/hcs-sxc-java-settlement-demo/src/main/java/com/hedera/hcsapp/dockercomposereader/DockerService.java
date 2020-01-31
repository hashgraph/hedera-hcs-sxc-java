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

import java.util.List;
import java.util.Map;

final public class DockerService {
    private String container_name;
    private Map<String, String> environment;
    private List<String> ports;
    
    public String getContainer_name() {
        return this.container_name;
    }
    public void setContainer_name(String container_name) {
        this.container_name = container_name;
    }
    public Map<String, String> getEnvironment() {
        return this.environment;
    }
    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }
    public List<String> getPorts() {
        return this.ports;
    }
    public void setPorts(List<String> ports) {
        this.ports = ports;
    }  
    public String getPort() {
        String[] ports = this.ports.get(0).split(":");
        return ports[0];
    }
    public int getPortAsInteger() {
        return Integer.parseInt(getPort());
    }
}
