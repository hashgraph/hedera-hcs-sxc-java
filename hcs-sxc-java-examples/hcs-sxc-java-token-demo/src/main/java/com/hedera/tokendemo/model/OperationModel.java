package com.hedera.tokendemo.model;

import com.hedera.tokendemo.domain.Account;
import com.hedera.tokendemo.domain.Operation;
import com.hedera.tokendemo.domain.Token;

import java.util.List;

public interface OperationModel {

    List<Operation> findByUserName(String userName);
    void create(String operator, String recipient, String operationData);

}