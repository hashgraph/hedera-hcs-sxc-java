package com.hedera.hcsapp.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public final class SettlementController {
    @RequestMapping("/settlements")
    public String index() {
        return "Greetings from the hcs settlement demo!";
    }
}
