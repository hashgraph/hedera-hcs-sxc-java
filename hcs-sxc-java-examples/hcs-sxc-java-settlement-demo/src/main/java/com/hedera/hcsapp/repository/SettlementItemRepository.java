package com.hedera.hcsapp.repository;

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

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.hedera.hcsapp.entities.SettlementItem;
import com.hedera.hcsapp.entities.SettlementItemId;

public interface SettlementItemRepository extends CrudRepository<SettlementItem, SettlementItemId> {
    
    @Query("SELECT si FROM SettlementItem si WHERE si.id.threadId = :threadId ORDER BY si.id.settledThreadId DESC")
    List<SettlementItem> findAllSettlementItems(@Param("threadId") String threadId);
}

