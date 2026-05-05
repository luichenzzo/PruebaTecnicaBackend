package com.example.optiplant.sse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseService.createEmitter();
    }

    // Convenience endpoint to broadcast an event to all connected clients (MANAGER/ADMIN only can be enforced later)
    @PostMapping("/publish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void publish(@RequestBody Object payload) {
        sseService.sendEvent(payload);
    }
}


