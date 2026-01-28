-- demo seed (won't overwrite occupied slots)
-- NOTE: Not idempotent overall (creates new plates each run), but only fills empty target slots.

WITH b AS (SELECT id FROM belt WHERE name='Main Belt' LIMIT 1),
s AS (
  SELECT id, row_number() OVER (ORDER BY position_index) rn
  FROM belt_slot
  WHERE belt_id=(SELECT id FROM b)
    AND plate_id IS NULL
    AND position_index % 8 = 0
  ORDER BY position_index
  LIMIT 24
),
m AS (
  SELECT id, default_tier, base_price_yen, row_number() OVER () rn
  FROM (SELECT id, default_tier, base_price_yen FROM menu_item ORDER BY random() LIMIT (SELECT count(*) FROM s)) x
),
p AS (
  INSERT INTO plate (id, menu_item_id, tier_snapshot, price_at_creation_yen, created_at, expires_at, status)
  SELECT uuid_generate_v4(), m.id, m.default_tier, m.base_price_yen, now(), now()+interval '2 hours', 'ON_BELT'
  FROM m
  RETURNING id
),
pr AS (
  SELECT id, row_number() OVER () rn FROM p
)
UPDATE belt_slot bs
SET plate_id = pr.id
FROM s JOIN pr USING (rn)
WHERE bs.id = s.id AND bs.plate_id IS NULL;