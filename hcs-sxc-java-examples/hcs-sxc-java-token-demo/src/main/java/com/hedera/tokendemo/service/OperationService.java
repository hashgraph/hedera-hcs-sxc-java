package com.hedera.tokendemo.service;

import com.hedera.tokendemo.domain.Operation;
import com.hedera.tokendemo.model.OperationModel;
import com.hedera.tokendemo.repository.OperationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OperationService implements OperationModel {
    private final OperationRepository operationRepository;

    public OperationService(OperationRepository operationRepository) {

        this.operationRepository = operationRepository;
    }

    @Override
    public List<Operation> findByUserName(String userName) {
        return operationRepository.findByUserName(userName);
    }

    @Override
    public void create(String operator, String recipient, String operationData) {
        Operation operation = new Operation();
        operation.setOperation(operationData);
        operation.setOperator(operator);
        operation.setRecipient(recipient);
        operationRepository.save(operation);
    }
}
