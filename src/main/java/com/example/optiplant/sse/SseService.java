package com.example.optiplant.sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages active server-sent event emitters and broadcasts payloads to them.
 */
@Service
public class SseService {

    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();

    /**
     * Creates and registers an SSE emitter for a connected client.
     *
     * @return registered emitter
     */
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(0L); // never timeout
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    /**
     * Sends a payload to every connected emitter and removes failed emitters.
     *
     * @param data payload to send
     */
    public void sendEvent(Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(data);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}

