DO $$
DECLARE
  ns UUID := 'b39e5310-6f6d-5608-83eb-d714e752abbd';
  ts TIMESTAMPTZ := '2026-01-27 00:00:00+00';

  belt_name TEXT := 'Main Belt';
  belt_uuid UUID := uuid_generate_v5(ns, 'belt:' || belt_name);

  slot_count INT := 192;
  tick_interval_ms INT := 250;
  speed_slots_per_tick INT := 1;
BEGIN

  -- 1) Belt (idempotent)
  INSERT INTO belt (
      id, name, slot_count, base_rotation_offset, offset_started_at, tick_interval_ms, speed_slots_per_tick
  )
  VALUES (
      belt_uuid, belt_name, slot_count, 0, ts, tick_interval_ms, speed_slots_per_tick
  )
  ON CONFLICT (id) DO UPDATE
    SET name = EXCLUDED.name,
        slot_count = EXCLUDED.slot_count,
        base_rotation_offset = EXCLUDED.base_rotation_offset,
        offset_started_at = EXCLUDED.offset_started_at,
        tick_interval_ms = EXCLUDED.tick_interval_ms,
        speed_slots_per_tick = EXCLUDED.speed_slots_per_tick;

  -- 2) Belt slots [0..191] (idempotent)
  INSERT INTO belt_slot (id, belt_id, position_index, plate_id)
  SELECT
      uuid_generate_v5(ns, 'belt_slot:' || belt_name || ':pos:' || gs.pos::text),
      belt_uuid,
      gs.pos,
      NULL
  FROM generate_series(0, slot_count - 1) AS gs(pos)
  ON CONFLICT (belt_id, position_index) DO NOTHING;

  -- 3) Seats (labels '1'..'24'), evenly spaced: (label-1)*8 (idempotent)
  INSERT INTO seat (id, label, belt_id, seat_position_index)
  SELECT
      uuid_generate_v5(ns, 'seat:' || belt_name || ':label:' || i::text),
      i::text,
      belt_uuid,
      (i - 1) * 8
  FROM generate_series(1, 24) AS i
  ON CONFLICT (belt_id, label) DO NOTHING;

END $$;
