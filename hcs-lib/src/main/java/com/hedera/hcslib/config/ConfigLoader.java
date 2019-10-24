package com.hedera.hcslib.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.hedera.hashgraph.sdk.account.AccountId;

/**
 * Manages configuration
 */

public final class ConfigLoader {
    private Config config = new Config();
    
    public ConfigLoader() throws FileNotFoundException, IOException {
        Yaml yaml = new Yaml(new Constructor(Config.class));
        
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("config.yaml");

        File configFile = new File("./config.yaml");
        if (configFile.exists()) {
            // config file exists outside of jar, use it
            inputStream = new FileInputStream(configFile.getCanonicalPath());
        }
        config = yaml.load(inputStream);
    }
    
    /** 
     * Returns a map of node details (AccountId and address)
     * @return Map<AccountId, String> 
     */
    public Map<AccountId, String> getNodesMap() {
        Map<AccountId, String> nodeList = new HashMap<AccountId, String>();
        
        for (Node node : config.getNodes()) {
            nodeList.put(AccountId.fromString(node.getAccount()), node.getAddress());
        }
        
        return nodeList;
    }
    
}
