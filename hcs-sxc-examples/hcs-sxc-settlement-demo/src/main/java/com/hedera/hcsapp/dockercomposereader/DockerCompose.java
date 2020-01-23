package com.hedera.hcsapp.dockercomposereader;

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
    public String getNameForId(long appId) {
        String name = "not found";
        for (Map.Entry<String, DockerService> service : this.services.entrySet()) {
            DockerService dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().containsKey("APP_ID")) {
                    if (dockerService.getEnvironment().get("APP_ID").contentEquals(String.valueOf(appId))) {
                        return dockerService.getContainer_name();
                    }
                }
            }
        }
        return name;
    }
    public String getPublicKeyForId(long appId) {
        String key = "not found";
        for (Map.Entry<String, DockerService> service : this.services.entrySet()) {
            DockerService dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().containsKey("PUBKEY")) {
                    if (dockerService.getEnvironment().get("APP_ID").contentEquals(String.valueOf(appId))) {
                        return dockerService.getContainer_name();
                    }
                }
            }
        }
        return key;
    }
    public int getPortForId(long appId) {
        int port = 0;
        for (Map.Entry<String, DockerService> service : this.services.entrySet()) {
            DockerService dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().get("APP_ID").contentEquals(String.valueOf(appId))) {
                    port = dockerService.getPortAsInteger();
                }
            }
        }
        return port;
    }
    public int getPortForId(String appId) {
        return getPortForId(Long.parseLong(appId));
    }
}
