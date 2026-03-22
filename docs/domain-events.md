# Sushi-Train Event Model

This document captures the event model used by the current application.
At `v0.2.0`, the production path for UI updates is Server-Sent Events (SSE), not a broker.

---

## 1) Current Event Channel (SSE)

Endpoint:

- `GET /api/v1/belts/{id}/events`
- Content type: `text/event-stream`

Current emitted SSE event names:

- `connected`
- `belt-state-changed`

Current payload type (`BeltUiEvent`):

```json
{
  "eventId": "UUID",
  "beltId": "UUID",
  "type": "belt-state-changed",
  "occurredAt": "2026-03-22T10:00:00Z"
}
```

Notes:

- `connected` is sent immediately after subscribe.
- `belt-state-changed` is sent after REST write operations that mutate belt/seat state.
- Payload is an invalidation signal; frontend reloads snapshot and seats via REST.

---

## 2) Publish Triggers (Current)

`belt-state-changed` is published after:

1. belt parameter updates (`PATCH /api/v1/belts/{id}`)
2. plate creation/placement (`POST /api/v1/belts/{id}/plates`)
3. seat occupy (`POST /api/v1/seats/{id}/occupy`)
4. pick plate (`POST /api/v1/seats/{id}/order-lines`)
5. seat checkout (`POST /api/v1/seats/{id}/checkout`)

---

## 3) Consumer Behavior

Frontend behavior in `belt-visualization.store.ts`:

- Try SSE first.
- On SSE events, refresh belt snapshot and seats.
- If SSE is unavailable/disconnected, fallback polling keeps data fresh.

This keeps realtime UX responsive while still robust under network/proxy limitations.

---

## 4) Domain Events vs UI Events

The current SSE stream carries UI refresh signals, not full domain event history.

If broker-based domain events are introduced later (Kafka/Redpanda), keep them separate from SSE UI events:

- SSE channel: low-latency UI invalidation/update triggers
- Broker topics: durable business events for integration, replay, analytics

Suggested principle for future broker usage:

- retain immutable business events (for example `OrderOpened`, `PlatePicked`, `OrderCheckedOut`)
- version event schemas explicitly
- partition by aggregate identity for ordering guarantees

---

## 5) Compatibility Contract

To avoid frontend breakage, keep these stable unless versioned:

- SSE endpoint path
- SSE event names
- `BeltUiEvent` JSON field names

If a breaking change is needed, version the endpoint or event shape and run both versions during transition.
