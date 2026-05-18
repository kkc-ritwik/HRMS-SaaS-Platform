package com.hrms.notification.controller;

import com.hrms.notification.dispatch.Channel;
import com.hrms.notification.dispatch.DispatchRequest;
import com.hrms.notification.dispatch.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationDispatchController {

    private final NotificationDispatcher dispatcher;

    /** Generic multi-channel dispatch entry point — used by all other services. */
    @PostMapping("/dispatch")
    public Map<String, String> dispatch(@RequestBody DispatchRequest req) {
        dispatcher.dispatch(req);
        return Map.of("status", "queued");
    }

    @GetMapping("/channels")
    public List<Channel> listChannels() { return dispatcher.allChannels(); }
}
