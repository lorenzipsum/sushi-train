-- BELT
CREATE TABLE belt (
    id                   UUID PRIMARY KEY,
    name                 TEXT NOT NULL,
    slot_count           INTEGER NOT NULL,
    base_rotation_offset INTEGER NOT NULL DEFAULT 0,
    offset_started_at    TIMESTAMPTZ NOT NULL,
    tick_interval_ms     INTEGER NOT NULL DEFAULT 1000,
    speed_slots_per_tick INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uk_belt_name UNIQUE (name)
);

-- SEAT
CREATE TABLE seat (
    id                   UUID PRIMARY KEY,
    label                TEXT NOT NULL,
    belt_id              UUID NOT NULL REFERENCES belt(id) ON DELETE RESTRICT,
    seat_position_index  INTEGER NOT NULL,
    CONSTRAINT uk_belt_label UNIQUE (belt_id, label),
    CONSTRAINT uk_seat_position UNIQUE (belt_id, seat_position_index),
    CONSTRAINT ck_seat_index_nonneg CHECK (seat_position_index >= 0)
);
CREATE INDEX idx_seat_belt ON seat(belt_id);

-- MENU ITEM
CREATE TABLE menu_item (
    id               UUID PRIMARY KEY,
    name             TEXT NOT NULL,
    default_tier     VARCHAR(16) NOT NULL,
    base_price_yen   INTEGER NOT NULL CHECK (base_price_yen >= 0),
    created_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_menu_item_name UNIQUE (name)
);
CREATE INDEX idx_menu_item_name ON menu_item(name);

-- PLATE
CREATE TABLE plate (
    id                      UUID PRIMARY KEY,
    menu_item_id            UUID NOT NULL REFERENCES menu_item(id) ON DELETE RESTRICT,
    tier_snapshot           VARCHAR(16) NOT NULL,
    price_at_creation_yen   INTEGER NOT NULL CHECK (price_at_creation_yen >= 0),
    created_at              TIMESTAMPTZ NOT NULL,
    expires_at              TIMESTAMPTZ NOT NULL,
    status                  VARCHAR(16) NOT NULL,
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_plate_expires_after_created CHECK (expires_at > created_at)
);
CREATE INDEX idx_plate_menu_item ON plate(menu_item_id);
CREATE INDEX idx_plate_status ON plate(status);
CREATE INDEX idx_plate_expires_at ON plate(expires_at);

-- ORDERS
CREATE TABLE orders (
    id          UUID PRIMARY KEY,
    seat_id     UUID NOT NULL REFERENCES seat(id) ON DELETE RESTRICT,
    status      VARCHAR(16) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    closed_at   TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_orders_closed_after_created CHECK (closed_at IS NULL OR closed_at >= created_at)
);
CREATE INDEX idx_orders_seat ON orders(seat_id);
CREATE INDEX idx_orders_status_created ON orders(status, created_at);
CREATE INDEX idx_orders_status ON orders(status);

-- ORDER LINE
CREATE TABLE order_line (
    id                         UUID PRIMARY KEY,
    plate_id                   UUID NOT NULL UNIQUE REFERENCES plate(id) ON DELETE RESTRICT,
    order_id                   UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_name_snapshot    TEXT NOT NULL,
    tier_snapshot              VARCHAR(16) NOT NULL,
    price_at_pick_yen          INTEGER NOT NULL CHECK (price_at_pick_yen >= 0),
    picked_at                  TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_order_line_order ON order_line(order_id);

-- BELT SLOT
CREATE TABLE belt_slot (
    id              UUID PRIMARY KEY,
    belt_id         UUID NOT NULL REFERENCES belt(id) ON DELETE CASCADE,
    position_index  INTEGER NOT NULL,
    plate_id        UUID UNIQUE REFERENCES plate(id) ON DELETE SET NULL,
    CONSTRAINT uk_belt_position UNIQUE (belt_id, position_index)
);
CREATE INDEX idx_belt_slot_belt ON belt_slot(belt_id);
CREATE INDEX idx_belt_slot_belt_position  ON belt_slot(belt_id, position_index);
