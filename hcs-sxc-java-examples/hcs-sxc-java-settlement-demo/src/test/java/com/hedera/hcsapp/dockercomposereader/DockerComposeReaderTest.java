package com.hedera.hcsapp.dockercomposereader;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DockerComposeReaderTest {    
    
    @Test
    public void testDockerComposeReader() throws Exception {
        DockerComposeReader reader = new DockerComposeReader();
        DockerCompose dockerCompose = reader.parse("./src/test/resources/docker-compose.yml");
        
        assertEquals("3.3", dockerCompose.getVersion());
        assertEquals("Alice", dockerCompose.getNameForId("Alice"));
        assertEquals("not found", dockerCompose.getNameForId("not found"));
        assertEquals(8081, dockerCompose.getPortForId("Alice"));
        assertEquals(0, dockerCompose.getPortForId("not found"));
        assertEquals("302a300506032b6570032100bcabbb31c4c6418ea323e02dd46c060f82936141b5d2d2a1da89e59e1267ab6b", dockerCompose.getPublicKeyForId("Alice"));
        assertEquals("not found", dockerCompose.getPublicKeyForId("not found"));
        
        Map<String, DockerService> services = dockerCompose.getServices();
        assertEquals(4, dockerCompose.getServices().size());
        
        assertThrows(Exception.class, () -> {DockerCompose failCompose = DockerComposeReader.parse("notAnExistingFile");});
    }
}
