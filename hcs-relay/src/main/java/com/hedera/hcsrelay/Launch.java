package com.hedera.hcsrelay;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.hedera.hcsrelay.subscribe.MirrorTopicsSubscriber;

public final class Launch {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        MirrorTopicsSubscriber topicsSubscriber = new MirrorTopicsSubscriber();
        
        System.out.println("Relay started");
        
        
    }

}
