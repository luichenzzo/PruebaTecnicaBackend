package com.example.optiplant.dto;

import java.util.UUID;

/**
 * Generic realtime event envelope sent over WebSocket topics.
 *
 * @param <T> payload type carried by the event
 */
public record RealtimeEvent<T>(
        String action,
        UUID id,
        T data
) {

    /**
     * Creates a creation event.
     *
     * @param id affected resource identifier
     * @param data event payload
     * @param <T> payload type
     * @return realtime event envelope
     */
    public static <T> RealtimeEvent<T> created(UUID id, T data) {
        return new RealtimeEvent<>("CREATED", id, data);
    }

    /**
     * Creates an update event.
     *
     * @param id affected resource identifier
     * @param data event payload
     * @param <T> payload type
     * @return realtime event envelope
     */
    public static <T> RealtimeEvent<T> updated(UUID id, T data) {
        return new RealtimeEvent<>("UPDATED", id, data);
    }

    /**
     * Creates a deletion event.
     *
     * @param id deleted resource identifier
     * @param <T> payload type
     * @return realtime event envelope with no payload
     */
    public static <T> RealtimeEvent<T> deleted(UUID id) {
        return new RealtimeEvent<>("DELETED", id, null);
    }
}
