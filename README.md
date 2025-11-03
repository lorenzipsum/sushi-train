# sushi-train

Application simulating a Sushi Train Restaurant

## Business Object Model

![Business Object Model](docs/business-objects.drawio.png)

## Use Cases

![Use Cases](docs/use-cases.drawio.png)

## ER diagram

```erDiagram
  %% Catalog vs instance
  MENU_ITEM {
    UUID id PK
    TEXT name
    TEXT default_tier
    INT  base_price_yen
    TIMESTAMPTZ created_at
  }

  PLATE {
    UUID id PK
    UUID menu_item_id FK
    TEXT tier_snapshot
    INT  price_at_creation_yen
    TIMESTAMPTZ created_at
    TIMESTAMPTZ expires_at
    TEXT status
  }

  %% Belt with rotation offset (movement modeled as state)
  BELT {
    UUID id PK
    TEXT name
    INT  slot_count
    INT  rotation_offset
    INT  tick_interval_ms
    INT  speed_slots_per_tick
  }

  BELT_SLOT {
    UUID id PK
    UUID belt_id FK
    INT  position_index
    UUID plate_id FK
  }

  %% Seat (optional spatial link)
  SEAT {
    UUID id PK
    TEXT label
    UUID belt_id FK
    INT  seat_position_index
  }

  %% Orders (rename from reserved word ORDER to ORDERS)
  ORDERS {
    UUID id PK
    UUID seat_id FK
    TEXT status
    TIMESTAMPTZ created_at
    TIMESTAMPTZ closed_at
  }

  ORDER_LINE {
    UUID id PK
    UUID order_id FK
    UUID plate_id FK
    TEXT menu_item_name_snapshot
    TEXT tier_snapshot
    INT  price_at_pick_yen
    TIMESTAMPTZ picked_at
  }

  %% Relationships
  MENU_ITEM ||--o{ PLATE : instantiates
  BELT      ||--o{ BELT_SLOT : has
  BELT_SLOT }o--|| PLATE : holds
  SEAT      }o--|| BELT : faces
  SEAT      ||--o{ ORDERS : opens
  ORDERS    ||--o{ ORDER_LINE : contains
  ORDER_LINE }o--|| PLATE : for_plate
```
