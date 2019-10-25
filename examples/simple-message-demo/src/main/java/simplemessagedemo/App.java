package simplemessagedemo;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import com.hedera.hcslib.outbound.OutboundHCSMessage;

/**
 * Hello world!
 *
 */
public final class App 
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        
        // example call back setup
//        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback();
//        onHCSMessageCallback.addObserver(message -> {
//            System.out.println("notified " + message);
//        });
//        onHCSMessageCallback.triggerCallBack();
        
        // HCSLib setup example
        HCSLib hcsLib = new HCSLib()
            .withEncryptedMessages(true)
            .withKeyRotation(true, 10)
            .withMessageSignature(true);
        
        // Outbound message (app->lib->hedera example)
        try {
            Boolean success = new OutboundHCSMessage(hcsLib)
                .overrideEncryptedMessages(true)
                .overrideKeyRotation(true, 10)
                .overrideMessageSignature(false)
                .sendMessage("test");
        } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
