package simplemessagedemo;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.demo.config.ConfigLoader;
import com.hedera.demo.config.Environment;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        ConfigLoader configLoader = new ConfigLoader();
        Environment environment = new Environment();
        
        // example call back setup
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback();
        onHCSMessageCallback.addObserver(message -> {
            System.out.println("notified " + message);
        });
        onHCSMessageCallback.triggerCallBack();
        
        HCSLib hcsLib = new HCSLib()
                .withEncryptedMessages(configLoader.config.getAppNet().getEncryptMessages())
                .withKeyRotation(configLoader.config.getAppNet().getRotateKeys(), configLoader.config.getAppNet().getRotateKeyFrequency())
                .withKmsSolution(configLoader.config.getAppNet().getKmsSolution())
                .withMessageSignature(configLoader.config.getAppNet().getSignMessages())
                .withQueueProtocol(configLoader.config.getAppNet().getQueueProtocol())
                .withOperatorAccountId(environment.getOperatorAccountId())
                .withOperatorKey(environment.getOperatorKey());
        
        try {
            hcsLib.sendMessage("testing");
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
