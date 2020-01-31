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

import com.hedera.hcsapp.entities.Settlement;

public interface SettlementRepository extends CrudRepository<Settlement, String> {
    @Query("SELECT s FROM Settlement s "
            + "WHERE (s.payerName = :currentUser AND s.recipientName = :searchUser) "
            + "OR (s.payerName = :searchUser AND s.recipientName = :currentUser) "
            + "OR (s.paymentChannelName = :currentUser AND s.payerName = :searchUser ) ORDER BY threadId DESC")
    
    List<Settlement> findAllSettlementsForUsers(@Param("currentUser") String currentUser, @Param("searchUser") String searchUser);

    @Query("SELECT s FROM Settlement s ORDER BY threadId DESC")
    List<Settlement> findAllDesc();
}

