package com.hedera.tokendemo.restcontrollers;

import com.hedera.tokendemo.config.AppData;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@CrossOrigin(maxAge = 3600)
@RestController
public class ApplicationController {

    private final AppData appData;

    static class ApplicationDetails {
        public String applicationName;
        public String topicId;
    }

    public ApplicationController(AppData appData) {
        this.appData = appData;
    }

    @GetMapping(value = "/application", produces = "application/json")
    public ApplicationDetails applicationDetails() {
        try {
            ApplicationDetails applicationDetails = new ApplicationDetails();
            applicationDetails.applicationName = appData.getUserName();
            applicationDetails.topicId = appData.getHCSCore().getTopics().get(0).getTopic();
            return applicationDetails;
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
}
