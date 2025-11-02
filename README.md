# sushi-train

Application simulating a Sushi Train Restaurant

## Business Object Model

![Business Object Model](docs\business-objects.drawio.png)

## Use Cases

![Use Cases](docs\use-cases.drawio.png)

## ER diagram

erDiagram
MENU_ITEM {
uuid id PK
text name
text tier // e.g., color/value category
int base_price_cents
timestamptz created_at
}

PLATE {
uuid id PK
uuid menu_item_id FK
text tier // denormalized snapshot
int price_cents // snapshot at creation (can differ from catalog)
timestamptz created_at
timestamptz expires_at
text status // ON_BELT, PICKED, EXPIRED
}

BELT {
uuid id PK
text name
int speed_ms // tick interval or slots/sec
}

BELT_SLOT {
uuid id PK
uuid belt_id FK
int position_index // 0..N-1
uuid plate_id FK NULL // slot may be empty
}

SEAT {
uuid id PK
text label // e.g., "A1"
}

"ORDER" {
uuid id PK
uuid seat_id FK
text status // OPEN, CHECKED_OUT, CANCELED
timestamptz created_at
timestamptz closed_at NULL
}

ORDER_LINE {
uuid id PK
uuid order_id FK
uuid plate_id FK
text menu_item_name // snapshot (optional but handy)
int price_at_pick_cents
timestamptz picked_at
}

%% Relationships (crow's foot)
MENU_ITEM ||--o{ PLATE : "instantiates"
BELT ||--o{ BELT_SLOT : "has"
BELT_SLOT }o--|| PLATE : "holds (0..1)"
SEAT ||--o{ "ORDER" : "opens (1 open max)\*"
"ORDER" ||--o{ ORDER_LINE : "contains"
ORDER_LINE }o--|| PLATE : "for plate"
