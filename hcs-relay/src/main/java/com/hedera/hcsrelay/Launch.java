package com.hedera.hcsrelay;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hcsrelay.subscribe.TopicsSubscriber;

public final class Launch {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        TopicsSubscriber topicsSubscriber = new TopicsSubscriber();
        
        System.out.println("Relay started");
        
        
    }

}
