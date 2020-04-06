package com.hedera.tokendemo.integration;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.hedera.hcs.sxc.consensus.OutboundHCSMessage;
import com.hedera.tokendemo.config.AppData;
import com.hedera.tokendemo.domain.Token;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.tti.ttf.taxonomy.model.artifact.*;
import org.tti.ttf.taxonomy.model.core.Base;

@Log4j2
@Component
public class HCSMessages {

    private final AppData appData;

    public HCSMessages(AppData appData) {
        this.appData = appData;
    }

    public void tokenCreate(Token token, String tokenTemplate, String userName) {
        NewArtifactRequest.Builder newArtifactRequest = NewArtifactRequest.newBuilder();
        
        Base base = Base.newBuilder()
                .setName(token.getName())
                .setSymbol(token.getSymbol())
                .setOwner(userName)
                .setQuantity(token.getCap())
                .setDecimals(token.getDecimals())
                .setConstructorName(tokenTemplate) // not quite right //TODO:
//              .setTokenUnit(TokenUnit.FRACTIONAL) // depends on the template
//              .setTokenType(TokenType.FUNGIBLE)// depends on the template
//              .setSupply(Supply.CAPPED_VARIABLE)// depends on the template
                .build();
        
        
        Any anyArtifactBase = Any.pack(base);
                
        newArtifactRequest.setType(ArtifactType.BASE);
        newArtifactRequest.setArtifact(anyArtifactBase);
        
        HCSToken hcsToken = HCSToken.newBuilder()
                .setNewArtifactRequest(newArtifactRequest.build())
                .build();

        sendHCSMessage(hcsToken.toByteArray());
    }

    public void tokenMint(String tokenName, long newQuantity, String userName) {
        HCSTokenOperation hcsTokenOperation = HCSTokenOperation.newBuilder()
                .setOperatorName(userName)
                .setTokenName(tokenName)
                .build();
                
        Any anyTokenOperation = Any.pack(hcsTokenOperation);
        
        MessageHeader messageHeader = MessageHeader.newBuilder()
                .setCustomMessageHeader(anyTokenOperation)
                .build();
        
        MintRequest mintRequest = MintRequest.newBuilder()
                .setQuantity(ByteString.copyFromUtf8(Long.toString(newQuantity)))
                .setHeader(messageHeader)
                .build();              
        
        HCSToken hcsToken = HCSToken.newBuilder()
                .setMintRequest(mintRequest)
                .build();

        sendHCSMessage(hcsToken.toByteArray());
    }
    
    public void tokenTransfer(String tokenName, String accountName, long amount, String userName) {
        HCSTokenOperation hcsTokenOperation = HCSTokenOperation.newBuilder()
                .setOperatorName(userName)
                .setTokenName(tokenName)
                .build();
                
        Any anyTokenOperation = Any.pack(hcsTokenOperation);
        
        MessageHeader messageHeader = MessageHeader.newBuilder()
                .setCustomMessageHeader(anyTokenOperation)
                .build();
        
        TransferRequest transferRequest = TransferRequest.newBuilder()
                .setQuantity(ByteString.copyFromUtf8(Long.toString(amount)))
                .setToAccountId(accountName)
                .setHeader(messageHeader)
                .build();              
        
        HCSToken hcsToken = HCSToken.newBuilder()
                .setTransferRequest(transferRequest)
                .build();

        sendHCSMessage(hcsToken.toByteArray());
    }
    public void tokenTransferFrom(String tokenName, String toAccountName, long amount, String fromAccountName) {
        HCSTokenOperation hcsTokenOperation = HCSTokenOperation.newBuilder()
                .setOperatorName(fromAccountName)
                .setTokenName(tokenName)
                .build();
                
        Any anyTokenOperation = Any.pack(hcsTokenOperation);
        
        MessageHeader messageHeader = MessageHeader.newBuilder()
                .setCustomMessageHeader(anyTokenOperation)
                .build();
        
        TransferFromRequest transferFromRequest = TransferFromRequest.newBuilder()
                .setQuantity(ByteString.copyFromUtf8(Long.toString(amount)))
                .setToAccountId(toAccountName)
                .setFromAccountId(fromAccountName)
                .setHeader(messageHeader)
                .build();              
        
        HCSToken hcsToken = HCSToken.newBuilder()
                .setTransferFromRequest(transferFromRequest)
                .build();

        sendHCSMessage(hcsToken.toByteArray());
    }
    public void tokenBurn(String tokenName, long burnQuantity, String userName) {
        HCSTokenOperation hcsTokenOperation = HCSTokenOperation.newBuilder()
                .setOperatorName(userName)
                .setTokenName(tokenName)
                .build();
                
        Any anyTokenOperation = Any.pack(hcsTokenOperation);
        
        MessageHeader messageHeader = MessageHeader.newBuilder()
                .setCustomMessageHeader(anyTokenOperation)
                .build();
        
        BurnRequest burnRequest = BurnRequest.newBuilder()
                .setQuantity(ByteString.copyFromUtf8(Long.toString(burnQuantity)))
                .setHeader(messageHeader)
                .build();              
        
        HCSToken hcsToken = HCSToken.newBuilder()
                .setBurnRequest(burnRequest)
                .build();

        sendHCSMessage(hcsToken.toByteArray());
    }
    private void sendHCSMessage(byte[] message) {
      try {
          // Send to HCS
          new OutboundHCSMessage(appData.getHCSCore())
              .sendMessage(0, message);
          System.out.println("");
          System.out.println("Request sent to HCS");
//          System.out.println(AppData.getPrompt());
          
          log.debug("Message sent to HCS successfully.");
      } catch (Exception e) {
          log.error(e);
          System.out.println("An error occurred - " + e.getMessage());
      }        
        
    }
}
