# Domain Model

This document reflects the persisted model defined by Flyway migrations (`V1__init_schema.sql`, `V2__seed_menu_items.sql`, `V3__seed_belt.sql`).

## Business Objects

![Business Object Model](business-objects.drawio.png)

## Use Cases

![Use Cases](use-cases.drawio.png)

## Entity Relationships

```mermaid
erDiagram
  MENU_ITEM {
    UUID id PK
    TEXT name
    VARCHAR default_tier
    INT base_price_yen
    TIMESTAMPTZ created_at
  }

  PLATE {
    UUID id PK
    UUID menu_item_id FK
    VARCHAR tier_snapshot
    INT price_at_creation_yen
    TIMESTAMPTZ created_at
    TIMESTAMPTZ expires_at
    VARCHAR status
    BIGINT version
  }

  BELT {
    UUID id PK
    TEXT name
    INT slot_count
    INT base_rotation_offset
    TIMESTAMPTZ offset_started_at
    INT tick_interval_ms
    INT speed_slots_per_tick
  }

  BELT_SLOT {
    UUID id PK
    UUID belt_id FK
    INT position_index
    UUID plate_id FK
  }

  SEAT {
    UUID id PK
    TEXT label
    UUID belt_id FK
    INT position_index
  }

  ORDERS {
    UUID id PK
    UUID seat_id FK
    VARCHAR status
    TIMESTAMPTZ created_at
    TIMESTAMPTZ closed_at
    BIGINT version
  }

  ORDER_LINE {
    UUID id PK
    UUID order_id FK
    UUID plate_id FK
    TEXT menu_item_name_snapshot
    VARCHAR tier_snapshot
    INT price_at_pick_yen
    TIMESTAMPTZ picked_at
  }

  MENU_ITEM ||--o{ PLATE : defines
  BELT ||--o{ BELT_SLOT : has_slots
  BELT_SLOT }o--|| PLATE : carries
  BELT ||--o{ SEAT : has_seats
  SEAT ||--o{ ORDERS : opens
  ORDERS ||--o{ ORDER_LINE : contains
  ORDER_LINE }o--|| PLATE : references
```

## Key Constraints

- `menu_item.name` is unique.
- One open order per seat (`uk_orders_open_per_seat` partial unique index).
- One plate can only be in one order line (`order_line.plate_id` unique).
- One plate can only occupy one belt slot at a time (`belt_slot.plate_id` unique).
- `belt_slot` uniqueness by `(belt_id, position_index)`.
- `seat` uniqueness by `(belt_id, label)` and `(belt_id, position_index)`.

## Seeded Baseline Data

- One belt named `Main Belt`.
- 192 belt slots (`position_index` `0..191`).
- 24 seats (`label` `1..24`) spaced around the belt.
- Menu items seeded with deterministic UUIDv5 values.
