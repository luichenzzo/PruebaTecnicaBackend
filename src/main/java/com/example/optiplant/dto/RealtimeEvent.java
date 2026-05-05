package com.example.optiplant.dto;

import java.util.UUID;

public record RealtimeEvent<T>(
        String action,
        UUID id,
        T data
) {

    public static <T> RealtimeEvent<T> created(UUID id, T data) {
        return new RealtimeEvent<>("CREATED", id, data);
    }

    public static <T> RealtimeEvent<T> updated(UUID id, T data) {
        return new RealtimeEvent<>("UPDATED", id, data);
    }

    public static <T> RealtimeEvent<T> deleted(UUID id) {
        return new RealtimeEvent<>("DELETED", id, null);
    }
}
