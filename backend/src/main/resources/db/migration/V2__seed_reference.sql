-- Enable UUID helpers (safe if already enabled in the database image used for fresh envs)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Create one belt with a 48s lap (192 slots × 250ms per tick)
INSERT INTO belt (id, name, slot_count, rotation_offset, tick_interval_ms, speed_slots_per_tick)
VALUES (
  '11111111-1111-1111-1111-111111111111',
  'Main Belt',
  192,
  0,
  250,
  1
);

-- 2) Create 192 belt slots [0..191]
INSERT INTO belt_slot (id, belt_id, position_index, plate_id)
SELECT uuid_generate_v4(),
       '11111111-1111-1111-1111-111111111111',
       gs.pos,
       NULL
FROM generate_series(0, 191) AS gs(pos);

-- 3) Create 24 seats (labels '1'..'24'), evenly spaced around the belt
-- position index = floor((label-1) * 192 / 24) = (label-1) * 8
INSERT INTO seat (id, label, belt_id, seat_position_index)
SELECT uuid_generate_v4(),
       (i)::text,
       '11111111-1111-1111-1111-111111111111',
       (i - 1) * 8
FROM generate_series(1, 24) AS i;

-- 4) Canonical menu items (tiers & base prices); created_at is deterministic “now()”
--    Keep this list stable; add new items via a new migration (V3, V4, …)
INSERT INTO menu_item (id, name, default_tier, base_price_yen, created_at) VALUES
  -- Nigiri
  ('11111111-1111-1111-1111-111111111111','Salmon Nigiri','GREEN',450, now()),
  (uuid_generate_v4(),'Tuna Nigiri','RED',550, now()),
  (uuid_generate_v4(),'Yellowtail Nigiri','RED',600, now()),
  (uuid_generate_v4(),'Ebi (Shrimp) Nigiri','GREEN',450, now()),
  (uuid_generate_v4(),'Unagi Nigiri','GOLD',800, now()),
  (uuid_generate_v4(),'Tamago Nigiri','GREEN',300, now()),
  (uuid_generate_v4(),'Saba Nigiri','GREEN',400, now()),
  (uuid_generate_v4(),'Ika (Squid) Nigiri','GREEN',400, now()),
  (uuid_generate_v4(),'Hotate (Scallop) Nigiri','RED',650, now()),
  (uuid_generate_v4(),'Kani (Crab) Nigiri','RED',600, now()),

  -- Rolls
  (uuid_generate_v4(),'California Roll','GREEN',450, now()),
  (uuid_generate_v4(),'Spicy Tuna Roll','RED',550, now()),
  (uuid_generate_v4(),'Kappa Maki (Cucumber)','GREEN',300, now()),
  (uuid_generate_v4(),'Tekka Maki (Tuna)','GREEN',400, now()),
  (uuid_generate_v4(),'Futomaki','RED',600, now()),

  -- Gunkan / Specials
  (uuid_generate_v4(),'Ikura (Salmon Roe) Gunkan','GOLD',900, now()),
  (uuid_generate_v4(),'Negitoro Gunkan','RED',650, now()),
  (uuid_generate_v4(),'Corn Mayo Gunkan','GREEN',350, now()),

  -- Sides / Hot dishes
  (uuid_generate_v4(),'Miso Soup','GREEN',200, now()),
  (uuid_generate_v4(),'Edamame','GREEN',300, now()),
  (uuid_generate_v4(),'Gyoza (5pc)','RED',500, now()),
  (uuid_generate_v4(),'Chicken Karaage','GOLD',800, now()),
  (uuid_generate_v4(),'Agedashi Tofu','GREEN',400, now()),

  -- Desserts
  (uuid_generate_v4(),'Mochi Ice Cream','GREEN',300, now()),
  (uuid_generate_v4(),'Purin (Custard)','GREEN',300, now()),
  (uuid_generate_v4(),'Cheesecake Slice','RED',450, now()),

  -- Drinks
  (uuid_generate_v4(),'Green Tea (Hot)','GREEN',150, now()),
  (uuid_generate_v4(),'Matcha Latte','RED',450, now()),
  (uuid_generate_v4(),'Cola','GREEN',200, now()),
  (uuid_generate_v4(),'Mineral Water','GREEN',150, now());
