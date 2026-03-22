package com.lorenzipsum.sushitrain.backend.interfaces.rest.belt;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class BeltEventStreamBroker {
    private final Map<UUID, List<SseEmitter>> emittersByBelt = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID beltId) {
        SseEmitter emitter = new SseEmitter(0L);
        emittersByBelt.computeIfAbsent(beltId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(beltId, emitter));
        emitter.onTimeout(() -> removeEmitter(beltId, emitter));
        emitter.onError(error -> removeEmitter(beltId, emitter));

        sendToEmitter(
                beltId,
                emitter,
                SseEmitter.event()
                        .name("connected")
                        .data(buildEvent(beltId, "connected"))
        );

        return emitter;
    }

    public void publish(UUID beltId, String type) {
        List<SseEmitter> emitters = emittersByBelt.get(beltId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        BeltUiEvent event = buildEvent(beltId, type);
        for (SseEmitter emitter : emitters) {
            sendToEmitter(
                    beltId,
                    emitter,
                    SseEmitter.event()
                            .name(type)
                            .data(event)
            );
        }
    }

    private BeltUiEvent buildEvent(UUID beltId, String type) {
        return new BeltUiEvent(
                UUID.randomUUID(),
                beltId,
                type,
                Instant.now()
        );
    }

    private void sendToEmitter(UUID beltId, SseEmitter emitter, SseEmitter.SseEventBuilder payload) {
        try {
            emitter.send(payload);
        } catch (Exception ex) {
            removeEmitter(beltId, emitter);
            try {
                emitter.completeWithError(ex);
            } catch (Exception ignored) {
                // Emitter may already be completed/closed; ignore cleanup failures.
            }
        }
    }

    private void removeEmitter(UUID beltId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByBelt.get(beltId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByBelt.remove(beltId);
        }
    }
}
