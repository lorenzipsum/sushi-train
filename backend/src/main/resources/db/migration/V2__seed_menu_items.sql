DO $$
DECLARE
  -- Namespace UUID for deterministic UUIDv5 generation.
  ns UUID := 'b39e5310-6f6d-5608-83eb-d714e752abbd';
  ts TIMESTAMPTZ := '2026-01-27 00:00:00+00';
BEGIN

  INSERT INTO menu_item (id, name, default_tier, base_price_yen, created_at)
  VALUES
    -- Nigiri
    (uuid_generate_v5(ns, 'menu_item:Salmon Nigiri'), 'Salmon Nigiri','GREEN',450, ts),
    (uuid_generate_v5(ns, 'menu_item:Tuna Nigiri'), 'Tuna Nigiri','RED',550, ts),
    (uuid_generate_v5(ns, 'menu_item:Yellowtail Nigiri'), 'Yellowtail Nigiri','RED',600, ts),
    (uuid_generate_v5(ns, 'menu_item:Ebi (Shrimp) Nigiri'), 'Ebi (Shrimp) Nigiri','GREEN',450, ts),
    (uuid_generate_v5(ns, 'menu_item:Unagi Nigiri'), 'Unagi Nigiri','GOLD',800, ts),
    (uuid_generate_v5(ns, 'menu_item:Tamago Nigiri'), 'Tamago Nigiri','GREEN',300, ts),
    (uuid_generate_v5(ns, 'menu_item:Saba Nigiri'), 'Saba Nigiri','GREEN',400, ts),
    (uuid_generate_v5(ns, 'menu_item:Ika (Squid) Nigiri'), 'Ika (Squid) Nigiri','GREEN',400, ts),
    (uuid_generate_v5(ns, 'menu_item:Hotate (Scallop) Nigiri'), 'Hotate (Scallop) Nigiri','RED',650, ts),
    (uuid_generate_v5(ns, 'menu_item:Kani (Crab) Nigiri'), 'Kani (Crab) Nigiri','RED',600, ts),

    -- Sashimi
    (uuid_generate_v5(ns, 'menu_item:Salmon Sashimi'), 'Salmon Sashimi','RED',700, ts),
    (uuid_generate_v5(ns, 'menu_item:Tuna Sashimi'), 'Tuna Sashimi','RED',800, ts),

    -- Rolls
    (uuid_generate_v5(ns, 'menu_item:California Roll'), 'California Roll','GREEN',450, ts),
    (uuid_generate_v5(ns, 'menu_item:Spicy Tuna Roll'), 'Spicy Tuna Roll','RED',550, ts),
    (uuid_generate_v5(ns, 'menu_item:Kappa Maki (Cucumber)'), 'Kappa Maki (Cucumber)','GREEN',300, ts),
    (uuid_generate_v5(ns, 'menu_item:Tekka Maki (Tuna)'), 'Tekka Maki (Tuna)','GREEN',400, ts),
    (uuid_generate_v5(ns, 'menu_item:Futomaki'), 'Futomaki','RED',600, ts),

    -- Gunkan / Specials
    (uuid_generate_v5(ns, 'menu_item:Ikura (Salmon Roe) Gunkan'), 'Ikura (Salmon Roe) Gunkan','GOLD',900, ts),
    (uuid_generate_v5(ns, 'menu_item:Negitoro Gunkan'), 'Negitoro Gunkan','RED',650, ts),
    (uuid_generate_v5(ns, 'menu_item:Corn Mayo Gunkan'), 'Corn Mayo Gunkan','GREEN',350, ts),

    -- Sides / Hot dishes
    (uuid_generate_v5(ns, 'menu_item:Miso Soup'), 'Miso Soup','GREEN',200, ts),
    (uuid_generate_v5(ns, 'menu_item:Edamame'), 'Edamame','GREEN',300, ts),
    (uuid_generate_v5(ns, 'menu_item:Gyoza (5pc)'), 'Gyoza (5pc)','RED',500, ts),
    (uuid_generate_v5(ns, 'menu_item:Chicken Karaage'), 'Chicken Karaage','GOLD',800, ts),
    (uuid_generate_v5(ns, 'menu_item:Agedashi Tofu'), 'Agedashi Tofu','GREEN',400, ts),

    -- Desserts
    (uuid_generate_v5(ns, 'menu_item:Mochi Ice Cream'), 'Mochi Ice Cream','GREEN',300, ts),
    (uuid_generate_v5(ns, 'menu_item:Purin (Custard)'), 'Purin (Custard)','GREEN',300, ts),
    (uuid_generate_v5(ns, 'menu_item:Cheesecake Slice'), 'Cheesecake Slice','RED',450, ts),

    -- Drinks
    (uuid_generate_v5(ns, 'menu_item:Green Tea (Hot)'), 'Green Tea (Hot)','GREEN',150, ts),
    (uuid_generate_v5(ns, 'menu_item:Matcha Latte'), 'Matcha Latte','RED',450, ts),
    (uuid_generate_v5(ns, 'menu_item:Cola'), 'Cola','GREEN',200, ts),
    (uuid_generate_v5(ns, 'menu_item:Mineral Water'), 'Mineral Water','GREEN',150, ts),
    (uuid_generate_v5(ns, 'menu_item:Asahi Super Dry Beer'), 'Asahi Super Dry Beer','GOLD',500, ts)

  ON CONFLICT (name) DO UPDATE
    SET default_tier   = EXCLUDED.default_tier,
        base_price_yen = EXCLUDED.base_price_yen
        , created_at     = EXCLUDED.created_at;

END $$;
