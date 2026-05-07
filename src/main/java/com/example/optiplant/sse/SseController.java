package com.example.optiplant.sse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller that exposes server-sent event streaming and manual publish
 * endpoints.
 */
@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Opens a server-sent event stream for the current client.
     *
     * @return SSE emitter registered with the service
     */
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseService.createEmitter();
    }

    /**
     * Broadcasts an arbitrary payload to all connected SSE clients.
     *
     * @param payload payload to broadcast
     */
    @PostMapping("/publish")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void publish(@RequestBody Object payload) {
        sseService.sendEvent(payload);
    }
}


