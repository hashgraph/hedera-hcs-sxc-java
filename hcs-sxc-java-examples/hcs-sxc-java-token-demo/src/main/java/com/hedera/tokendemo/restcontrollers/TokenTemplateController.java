package com.hedera.tokendemo.restcontrollers;

import com.hedera.tokendemo.service.TokenTemplateService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@CrossOrigin(maxAge = 3600)
@RestController
public class TokenTemplateController {

    class Template {
        public String templateName;
    }

    private final TokenTemplateService tokenTemplateService;

    public TokenTemplateController(TokenTemplateService tokenTemplateService) throws Exception {
        this.tokenTemplateService = tokenTemplateService;
    }
    @GetMapping(value = "/templates", produces = "application/json")
    public List<Template> tokenTemplates() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, String> templateList = tokenTemplateService.list();

        List<Template> allTemplates = new ArrayList<Template>();
        for (Map.Entry<String, String> template : templateList.entrySet()) {
            Template templateItem = new Template();
            templateItem.templateName = template.getValue();
            allTemplates.add(templateItem);
        }

        return allTemplates;
    }
}
