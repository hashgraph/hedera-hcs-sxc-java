package com.hedera.hcs.sxc.interfaces;

import com.hedera.hcs.sxc.commonobjects.HCSResponse;

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

/**
 * 
 * Callback interface so apps can register to receive messages from the core component
 *
 */
public interface HCSCallBackToAppInterface  {  
    /**
     * Functional interface method which will be called when a new message 
     * needs to be notified to the app
     * @param hcsResponse the hcs message to notify
     */
    void onMessage(HCSResponse hcsResponse);
}
