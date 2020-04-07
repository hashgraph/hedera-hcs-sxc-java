package com.hedera.tokendemo.integration;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.hedera.hcs.sxc.callback.OnHCSMessageCallback;
import com.hedera.hcs.sxc.commonobjects.HCSResponse;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.tokendemo.config.AppData;
import com.hedera.tokendemo.service.TokenServiceProto;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.tti.ttf.taxonomy.model.artifact.HCSToken;

@Component
@Log4j2
public class HCSIntegration {

    private AppData appData;
    private TokenServiceProto tokenServiceProto;

    public HCSIntegration(AppData appData, TokenServiceProto tokenServiceProto) throws Exception {
        this.appData = appData;
        this.tokenServiceProto = tokenServiceProto;

        // create a callback object to receive the message
        if (appData.getHCSCore() == null) {
            log.error("HCS Core is null");
            throw new Exception("HCS Core is null");
        }
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(appData.getHCSCore());
        onHCSMessageCallback.addObserver((sxcConsensusMesssage, hcsResponse) ->  {
            processHCSMessage(sxcConsensusMesssage, hcsResponse);
        });
    }

    public void processHCSMessage(SxcConsensusMessage sxcConsensusMessage, HCSResponse hcsResponse) {
        try {
            HCSToken hcsToken = HCSToken.parseFrom(hcsResponse.getMessage());
            if (hcsToken.hasNewArtifactRequest()) {
                tokenServiceProto.create(hcsToken.getNewArtifactRequest());
                System.out.println("");
                System.out.println("Token creation successful.");
            } else if (hcsToken.hasMintRequest()) {
                tokenServiceProto.mint(hcsToken.getMintRequest());
                System.out.println("");
                System.out.println("Token minting successful.");
            } else if (hcsToken.hasTransferRequest()) {
                tokenServiceProto.transfer(hcsToken.getTransferRequest());
                System.out.println("");
                System.out.println("Token transfer successful.");
            } else if (hcsToken.hasTransferFromRequest()) {
                tokenServiceProto.transferFrom(hcsToken.getTransferFromRequest());
                System.out.println("");
                System.out.println("Token transfer from successful.");
            } else if (hcsToken.hasBurnRequest()) {
                tokenServiceProto.burn(hcsToken.getBurnRequest());
                System.out.println("");
                System.out.println("Token burn successful.");
            }
        } catch (Exception e) {
            System.out.println("");
            System.out.println(e.getMessage());
        }
        System.out.println(appData.getPrompt());
    }
}
