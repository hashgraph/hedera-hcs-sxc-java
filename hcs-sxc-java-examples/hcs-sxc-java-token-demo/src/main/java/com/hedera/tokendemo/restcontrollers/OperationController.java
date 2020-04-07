package com.hedera.tokendemo.restcontrollers;

import com.hedera.tokendemo.domain.Operation;
import com.hedera.tokendemo.service.OperationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@CrossOrigin(maxAge = 3600)
@RestController
public class OperationController {

    private final OperationService operationService;

    public OperationController(OperationService operationService) throws Exception {
        this.operationService = operationService;
    }

    @GetMapping(value = "/operations/{userName}", produces = "application/json")
    public List<Operation> tokens(@PathVariable String userName) {
        List<Operation> operations = operationService.findByUserName(userName);
        return operations;
    }
}
