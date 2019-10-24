package simplemessagedemo;

import com.hedera.hcslib.callback.OnHCSMessageCallback;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args)
    {
        // example call back setup
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback();
        onHCSMessageCallback.addObserver(message -> {
            System.out.println("notified " + message);
        });
        onHCSMessageCallback.triggerCallBack();
    }

}
