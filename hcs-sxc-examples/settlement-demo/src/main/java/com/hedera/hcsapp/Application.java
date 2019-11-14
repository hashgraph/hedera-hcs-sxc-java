package com.hedera.hcsapp;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hcsapp.appconfig.AppConfig;
import com.hedera.hcsapp.repository.CreditRepository;
import com.hedera.hcslib.HCSLib;
import com.hedera.hcslib.callback.OnHCSMessageCallback;
import com.hedera.hcslib.consensus.OutboundHCSMessage;
import com.hedera.hcsapp.entities.Credit;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import javax.annotation.Resource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Resource
    CreditRepository creditRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

//            System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//
//            long appId = 0;
//            
//            int topicIndex = 0; // refers to the first topic ID in the config.yaml
            
            // Simplest setup and send
//            AppConfig config = new AppConfig();
//            Dotenv dotEnv = Dotenv.configure().ignoreIfMissing().load();
//            HCSLib hcsLib = new HCSLib(appId);

//            appId = Long.parseLong(dotEnv.get("APPID"));
            
//            System.out.println("****************************************");
//            System.out.println("** Welcome to a simple HCS demo");
//            System.out.println("** I am app: " + config.getConfig().getAppClients().get((int) appId).getClientName());
//            System.out.println("****************************************");
//            
//            System.out.println(creditRepository.count());
//            
            // create a callback object to receive the message
//            OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsLib);
//            onHCSMessageCallback.addObserver(message -> {
//                System.out.println("Received : "+ message);
//            });

//            Boolean messageSuccess;
//            try {
//                messageSuccess = new OutboundHCSMessage(hcsLib)
//                        .overrideEncryptedMessages(false)
//                        .overrideMessageSignature(false)
//                        .sendMessage(topicIndex, userInput);
    //
//                if (messageSuccess) {
//                    System.out.println("Message sent successfully.");
//                }
//            } catch (HederaNetworkException | IllegalArgumentException | HederaException e) {
//                e.printStackTrace();
//            }
            
        };
    }    
}       
