package com.hedera.hcsapp.dockercomposereader;

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