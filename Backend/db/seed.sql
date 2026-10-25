-- Starting data for SpartanFinds. Run once, after schema.sql, against an EMPTY Category table.
--   mysql -u <user> -p < Backend/db/seed.sql
-- Running it a second time fails on the UNIQUE category_name and changes nothing.

USE spartan_finds;

-- The categories shown in the "Post an item" dropdown and the catalog filter.
INSERT INTO Category (category_name) VALUES
    ('Backpacks & Bags'),
    ('Electronics'),
    ('Keys'),
    ('ID & Cards'),
    ('Clothing'),
    ('Water Bottles & Drinkware'),
    ('Books & Notebooks'),
    ('Wallets & Money'),
    ('Jewelry & Accessories'),
    ('Other');
