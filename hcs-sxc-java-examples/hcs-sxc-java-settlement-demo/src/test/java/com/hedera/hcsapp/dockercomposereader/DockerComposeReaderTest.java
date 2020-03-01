package com.hedera.hcsapp.dockercomposereader;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DockerComposeReaderTest {    
    
    @Test
    public void testDockerComposeReader() throws Exception {

        DockerCompose dockerCompose = DockerComposeReader.parse("./src/test/resources/docker-compose.yml");
        
        assertEquals("3.3", dockerCompose.getVersion());
        assertEquals("Alice", dockerCompose.getNameForId("Alice"));
        assertEquals("not found", dockerCompose.getNameForId("not found"));
        assertEquals(8081, dockerCompose.getPortForId("Alice"));
        assertEquals(0, dockerCompose.getPortForId("not found"));
        assertEquals("302a300506032b65700321000c5fd53530c52e9950e98932e2bdc35c6f9cad0069198da76a611e28d4fe434b", dockerCompose.getPublicKeyForId("Alice"));
        assertEquals("not found", dockerCompose.getPublicKeyForId("not found"));
        
        assertEquals(4, dockerCompose.getServices().size());
        
        assertThrows(Exception.class, () -> {DockerComposeReader.parse("notAnExistingFile");});
    }
}
