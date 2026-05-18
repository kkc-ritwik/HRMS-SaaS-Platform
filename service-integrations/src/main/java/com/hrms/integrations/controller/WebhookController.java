package com.hrms.integrations.controller;

import com.hrms.integrations.entity.WebhookSubscription;
import com.hrms.integrations.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/integrations/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService svc;

    @PostMapping public WebhookSubscription create(@RequestBody WebhookSubscription s) { return svc.subscribe(s); }
    @GetMapping  public List<WebhookSubscription> list() { return svc.list(); }
}
