package com.hedera.hcsapp.dockercomposereader;

import java.util.Map;

public final class DockerCompose {
    private String version;
    private Map<String, Service> services;
    
    public String getVersion() {
        return this.version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public Map<String, Service> getServices() {
        return this.services;
    }
    public void setServices(Map<String, Service> services) {
        this.services = services;
    }
    public String getNameForId(long appId) {
        String name = "not found";
        for (Map.Entry<String, Service> service : this.services.entrySet()) {
            Service dockerService = service.getValue();
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
        String name = "not found";
        for (Map.Entry<String, Service> service : this.services.entrySet()) {
            Service dockerService = service.getValue();
            if (dockerService.getEnvironment() != null) {
                if (dockerService.getEnvironment().containsKey("PUBKEY")) {
                    if (dockerService.getEnvironment().get("APP_ID").contentEquals(String.valueOf(appId))) {
                        return dockerService.getContainer_name();
                    }
                }
            }
        }
        return name;
    }
}
