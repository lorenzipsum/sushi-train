-- Demo/local repeatable seeding:
-- - Keeps belt occupancy around ~15–25% by topping up empty slots
-- - Weighted randomness (popular items more frequent)
-- - Ensures category coverage (at least one plate for each category if absent)
-- NOTE: Designed for local/demo. Do not use in production environments.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE
  v_belt UUID;
  v_slot_count INT;
  v_current_occupied INT;
  v_current_occupancy NUMERIC;
  v_target_occupancy NUMERIC;
  v_target_count INT;
  v_to_add INT;
  v_needed INT;

  -- weighted pick
  v_total_weight INT;
  v_rand NUMERIC;

  v_menu_item_id UUID;
  v_tier TEXT;
  v_price INT;

  v_plate_id UUID;
  v_slot_id UUID;
  v_slot_pos INT;

  -- categories
  cat TEXT;
BEGIN
  -- Find the main belt
  SELECT id, slot_count INTO v_belt, v_slot_count
  FROM belt
  WHERE name = 'Main Belt'
  LIMIT 1;

  IF v_belt IS NULL THEN
    RAISE NOTICE 'Main Belt not found; run V2__seed_core_data.sql first.';
    RETURN;
  END IF;

  -- Current occupancy
  SELECT COUNT(*) INTO v_current_occupied
  FROM belt_slot
  WHERE belt_id = v_belt AND plate_id IS NOT NULL;

  v_current_occupancy := v_current_occupied::numeric / v_slot_count;

  -- Pick a random target in [0.15, 0.25]
  v_target_occupancy := 0.15 + (random() * 0.10);
  v_target_count := CEIL(v_target_occupancy * v_slot_count)::int;

  v_to_add := GREATEST(0, v_target_count - v_current_occupied);
  IF v_to_add = 0 THEN
    RAISE NOTICE 'Occupancy %.2f%% meets/exceeds target (current: %, target: %). No top-up.',
      v_current_occupancy*100, v_current_occupied, v_target_count;
    RETURN;
  END IF;

  -- Build weighted menu list (temp)
  CREATE TEMP TABLE tmp_weighted_items AS
  SELECT mi.id,
         mi.name,
         mi.default_tier::text AS tier,
         mi.base_price_yen     AS price,
         COALESCE((
           SELECT CASE
             WHEN mi.name ILIKE '%Salmon%' THEN 10
             WHEN mi.name ILIKE '%Tuna%' THEN 8
             WHEN mi.name ILIKE '%Yellowtail%' THEN 6
             WHEN mi.name ILIKE '%Ebi%' THEN 6
             WHEN mi.name ILIKE '%Tamago%' THEN 5
             WHEN mi.name ILIKE '%Saba%' THEN 5
             WHEN mi.name ILIKE '%Ika%' THEN 4
             WHEN mi.name ILIKE '%Hotate%' THEN 5
             WHEN mi.name ILIKE '%Kani%' THEN 4
             WHEN mi.name ILIKE '%California Roll%' THEN 6
             WHEN mi.name ILIKE '%Spicy Tuna Roll%' THEN 6
             WHEN mi.name ILIKE '%Kappa%' THEN 4
             WHEN mi.name ILIKE '%Tekka%' THEN 4
             WHEN mi.name ILIKE '%Futomaki%' THEN 4
             WHEN mi.name ILIKE '%Ikura%' THEN 3
             WHEN mi.name ILIKE '%Negitoro%' THEN 4
             WHEN mi.name ILIKE '%Corn Mayo%' THEN 3
             WHEN mi.name ILIKE '%Miso%' THEN 3
             WHEN mi.name ILIKE '%Edamame%' THEN 4
             WHEN mi.name ILIKE '%Gyoza%' THEN 3
             WHEN mi.name ILIKE '%Karaage%' THEN 3
             WHEN mi.name ILIKE '%Agedashi%' THEN 3
             WHEN mi.name ILIKE '%Mochi%' THEN 2
             WHEN mi.name ILIKE '%Purin%' THEN 2
             WHEN mi.name ILIKE '%Cheesecake%' THEN 2
             ELSE 1
           END
         ), 1) AS weight
  FROM menu_item mi;

  SELECT COALESCE(SUM(weight),0) INTO v_total_weight FROM tmp_weighted_items;
  IF v_total_weight = 0 THEN
    RAISE NOTICE 'No menu items found; run V2__seed_core_data.sql first.';
    RETURN;
  END IF;

  -- Ensure category coverage: if belt has zero from a category, add one first (if we still have slots to add).
  CREATE TEMP TABLE tmp_empty_slots AS
  SELECT id, position_index
  FROM belt_slot
  WHERE belt_id = v_belt AND plate_id IS NULL
  ORDER BY random();

  -- Helper to add 1 plate of a category if none present
  PERFORM 1;

  FOR cat IN SELECT * FROM (VALUES
    ('nigiri'),
    ('rolls'),
    ('gunkan'),
    ('sides'),
    ('desserts')
  ) AS cats(cat)
  LOOP
    EXIT WHEN v_to_add <= 0;

    -- Does the belt currently have this category?
    IF cat = 'nigiri' THEN
      IF NOT EXISTS (
        SELECT 1 FROM plate p
        JOIN menu_item mi ON mi.id = p.menu_item_id
        WHERE p.status = 'ON_BELT'
          AND (mi.name ILIKE '%Nigiri%')
      ) THEN
        -- pick a nigiri item
        SELECT id, tier, price
        INTO v_menu_item_id, v_tier, v_price
        FROM tmp_weighted_items
        WHERE name ILIKE '%Nigiri%'
        ORDER BY random()
        LIMIT 1;

        -- consume one empty slot
        SELECT id, position_index INTO v_slot_id, v_slot_pos
        FROM tmp_empty_slots
        LIMIT 1;

        IF v_slot_id IS NOT NULL THEN
          v_plate_id := uuid_generate_v4();
          INSERT INTO plate (id, menu_item_id, tier_snapshot, price_at_creation_yen, created_at, expires_at, status)
          VALUES (v_plate_id, v_menu_item_id, v_tier, v_price, now(), now() + interval '2 hours', 'ON_BELT');

          UPDATE belt_slot SET plate_id = v_plate_id WHERE id = v_slot_id AND plate_id IS NULL;

          DELETE FROM tmp_empty_slots WHERE id = v_slot_id;
          v_to_add := v_to_add - 1;
        END IF;
      END IF;

    ELSIF cat = 'rolls' THEN
      IF NOT EXISTS (
        SELECT 1 FROM plate p
        JOIN menu_item mi ON mi.id = p.menu_item_id
        WHERE p.status = 'ON_BELT'
          AND (mi.name ILIKE '%Roll%' OR mi.name ILIKE '%Maki%')
      ) THEN
        SELECT id, tier, price
        INTO v_menu_item_id, v_tier, v_price
        FROM tmp_weighted_items
        WHERE (name ILIKE '%Roll%' OR name ILIKE '%Maki%')
        ORDER BY random()
        LIMIT 1;

        SELECT id, position_index INTO v_slot_id, v_slot_pos
        FROM tmp_empty_slots
        LIMIT 1;

        IF v_slot_id IS NOT NULL THEN
          v_plate_id := uuid_generate_v4();
          INSERT INTO plate (id, menu_item_id, tier_snapshot, price_at_creation_yen, created_at, expires_at, status)
          VALUES (v_plate_id, v_menu_item_id, v_tier, v_price, now(), now() + interval '2 hours', 'ON_BELT');

          UPDATE belt_slot SET plate_id = v_plate_id WHERE id = v_slot_id AND plate_id IS NULL;

          DELETE FROM tmp_empty_slots WHERE id = v_slot_id;
          v_to_add := v_to_add - 1;
        END IF;
      END IF;

    ELSIF cat = 'gunkan' THEN
      IF NOT EXISTS (
        SELECT 1 FROM plate p
        JOIN menu_item mi ON mi.id = p.menu_item_id
        WHERE p.status = 'ON_BELT'
          AND (mi.name ILIKE '%Gunkan%')
      ) THEN
        SELECT id, tier, price
        INTO v_menu_item_id, v_tier, v_price
        FROM tmp_weighted_items
        WHERE name ILIKE '%Gunkan%'
        ORDER BY random()
        LIMIT 1;

        SELECT id, position_index INTO v_slot_id, v_slot_pos
        FROM tmp_empty_slots
        LIMIT 1;

        IF v_slot_id IS NOT NULL THEN
          v_plate_id := uuid_generate_v4();
          INSERT INTO plate (id, menu_item_id, tier_snapshot, price_at_creation_yen, created_at, expires_at, status)
          VALUES (v_plate_id, v_menu_item_id, v_tier, v_price, now(), now() + interval '2 hours', 'ON_BELT');

          UPDATE belt_slot SET plate_id = v_plate_id WHERE id = v_slot_id AND plate_id IS NULL;

          DELETE FROM tmp_empty_slots WHERE id = v_slot_id;
          v_to_add := v_to_add - 1;
        END IF;
      END IF;

    ELSIF cat = 'sides' THEN
      IF NOT EXISTS (
        SELECT 1 FROM plate p
        JOIN menu_item mi ON mi.id = p.menu_item_id
        WHERE p.status = 'ON_BELT'
          AND (mi.name ILIKE '%Miso%' OR mi.name ILIKE '%Edamame%' OR mi.name ILIKE '%Gyoza%' OR mi.name ILIKE '%Karaage%' OR mi.name ILIKE '%Agedashi%')
      ) THEN
        SELECT id, tier, price
        INTO v_menu_item_id, v_tier, v_price
        FROM tmp_weighted_items
        WHERE (name ILIKE '%Miso%' OR name ILIKE '%Edamame%' OR name ILIKE '%Gyoza%' OR name ILIKE '%Karaage%' OR name ILIKE '%Agedashi%')
        ORDER BY random()
        LIMIT 1;

        SELECT id, position_index INTO v_slot_id, v_slot_pos
        FROM tmp_empty_slots
        LIMIT 1;

        IF v_slot_id IS NOT NULL THEN
          v_plate_id := uuid_generate_v4();
          INSERT INTO plate (id, menu_item_id, tier_snapshot, price_at_creation_yen, created_at, expires_at, status)
          VALUES (v_plate_id, v_menu_item_id, v_tier, v_price, now(), now() + interval '2 hours', 'ON_BELT');

          UPDATE belt_slot SET plate_id = v_plate_id WHERE id = v_slot_id AND plate_id IS NULL;

          DELETE FROM tmp_empty_slots WHERE id = v_slot_id;
          v_to_add := v_to_add - 1;
        END IF;
      END IF;

    ELSIF cat = 'desserts' THEN
      IF NOT EXISTS (
        SELECT 1 FROM plate p
        JOIN menu_item mi ON mi.id = p.menu_item_id
        WHERE p.status = 'ON_BELT'
          AND (mi.name ILIKE '%Mochi%' OR mi.name ILIKE '%Purin%' OR mi.name ILIKE '%Cheesecake%')
      ) THEN
        SELECT id, tier, price
        INTO v_menu_item_id, v_tier, v_price
        FROM tmp_weighted_items
        WHERE (name ILIKE '%Mochi%' OR name ILIKE '%Purin%' OR name ILIKE '%Cheesecake%')
        ORDER BY random()
        LIMIT 1;

        SELECT id, position_index INTO v_slot_id, v_slot_pos
        FROM tmp_empty_slots
        LIMIT 1;

        IF v_slot_id IS NOT NULL THEN
          v_plate_id := uuid_generate_v4();
          INSERT INTO plate (id, menu_item_id, tier_snapshot, price_at_creation_yen, created_at, expires_at, status)
          VALUES (v_plate_id, v_menu_item_id, v_tier, v_price, now(), now() + interval '2 hours', 'ON_BELT');

          UPDATE belt_slot SET plate_id = v_plate_id WHERE id = v_slot_id AND plate_id IS NULL;

          DELETE FROM tmp_empty_slots WHERE id = v_slot_id;
          v_to_add := v_to_add - 1;
        END IF;
      END IF;
    END IF;
  END LOOP;

  -- 2) Random weighted fill for the remaining needed plates
  IF v_to_add > 0 THEN
    -- Recompute remaining empty slots pool
    DELETE FROM tmp_empty_slots;  -- clear
    INSERT INTO tmp_empty_slots(id, position_index)
    SELECT id, position_index
    FROM belt_slot
    WHERE belt_id = v_belt AND plate_id IS NULL
    ORDER BY random()
    LIMIT v_to_add;

    FOR v_slot_id, v_slot_pos IN
      SELECT id, position_index FROM tmp_empty_slots
    LOOP
      -- weighted pick
      v_rand := random() * v_total_weight;

      WITH ranked AS (
        SELECT id, tier, price,
               SUM(weight) OVER (ORDER BY id) AS cum_w
        FROM tmp_weighted_items
        ORDER BY id
      )
      SELECT id, tier, price
        INTO v_menu_item_id, v_tier, v_price
      FROM ranked
      WHERE cum_w >= v_rand
      ORDER BY cum_w
      LIMIT 1;

      IF v_menu_item_id IS NULL THEN
        -- fallback
        SELECT id, tier, price
          INTO v_menu_item_id, v_tier, v_price
        FROM tmp_weighted_items
        ORDER BY random()
        LIMIT 1;
      END IF;

      v_plate_id := uuid_generate_v4();
      INSERT INTO plate (id, menu_item_id, tier_snapshot, price_at_creation_yen, created_at, expires_at, status)
      VALUES (v_plate_id, v_menu_item_id, v_tier, v_price, now(), now() + interval '2 hours', 'ON_BELT');

      UPDATE belt_slot
         SET plate_id = v_plate_id
       WHERE id = v_slot_id
         AND plate_id IS NULL;
    END LOOP;
  END IF;

  RAISE NOTICE 'Demo stock top-up complete.';

END $$;
