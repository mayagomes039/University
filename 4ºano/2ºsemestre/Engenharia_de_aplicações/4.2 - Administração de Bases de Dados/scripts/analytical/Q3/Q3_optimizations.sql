CREATE INDEX IF NOT EXISTS idx_publisher_name ON publisher(name);
CREATE INDEX IF NOT EXISTS idx_developer_name ON developer(name);
CREATE INDEX IF NOT EXISTS idx_games_publishers_pub_game ON games_publishers(publisher_id, game_id);
CREATE INDEX IF NOT EXISTS idx_games_developers_dev_game ON games_developers(developer_id, game_id);
CREATE INDEX IF NOT EXISTS idx_library_game_user ON library(game_id, user_id);
CREATE INDEX IF NOT EXISTS idx_library_user_game ON library(user_id, game_id);

CREATE TABLE IF NOT EXISTS company_user_game_stats (
    company_name TEXT PRIMARY KEY,
    total INT,
    unique_users INT,
    last_updated TIMESTAMP DEFAULT NOW()
);

WITH publisher_user_games AS (
    SELECT p.name AS company_name, l.user_id, l.game_id
    FROM games_publishers gp
    JOIN publisher p ON p.id = gp.publisher_id
    JOIN library l ON l.game_id = gp.game_id
),
developer_user_games AS (
    SELECT d.name AS company_name, l.user_id, l.game_id
    FROM games_developers gd
    JOIN developer d ON d.id = gd.developer_id
    JOIN library l ON l.game_id = gd.game_id
),
combined_user_games AS (
    SELECT * FROM publisher_user_games
    UNION ALL
    SELECT * FROM developer_user_games
),
aggregated_stats AS (
    SELECT
        company_name,
        COUNT(*) AS total,
        COUNT(DISTINCT user_id) AS unique_users
    FROM combined_user_games
    GROUP BY company_name
)
INSERT INTO company_user_game_stats (company_name, total, unique_users, last_updated)
SELECT
    company_name, total, unique_users, NOW()
FROM aggregated_stats
ON CONFLICT (company_name) DO UPDATE
SET total = EXCLUDED.total,
    unique_users = EXCLUDED.unique_users,
    last_updated = NOW();

CREATE OR REPLACE FUNCTION update_company_game_stats_on_insert()
RETURNS TRIGGER AS $$
BEGIN
    -- Publishers
    INSERT INTO company_user_game_stats (company_name, total, unique_users, last_updated)
    SELECT p.name, 1, 1, NOW()
    FROM games_publishers gp
    JOIN publisher p ON p.id = gp.publisher_id
    WHERE gp.game_id = NEW.game_id
    ON CONFLICT (company_name) DO UPDATE
    SET total = company_user_game_stats.total + 1,
        unique_users = company_user_game_stats.unique_users + 
            CASE 
                WHEN EXISTS (
                    SELECT 1 FROM library l
                    JOIN games_publishers gp2 ON l.game_id = gp2.game_id
                    JOIN publisher p2 ON p2.id = gp2.publisher_id
                    WHERE l.user_id = NEW.user_id
                      AND gp2.game_id != NEW.game_id
                      AND p2.name = company_user_game_stats.company_name
                ) THEN 0
                ELSE 1
            END,
        last_updated = NOW();

    -- Developers
    INSERT INTO company_user_game_stats (company_name, total, unique_users, last_updated)
    SELECT d.name, 1, 1, NOW()
    FROM games_developers gd
    JOIN developer d ON d.id = gd.developer_id
    WHERE gd.game_id = NEW.game_id
    ON CONFLICT (company_name) DO UPDATE
    SET total = company_user_game_stats.total + 1,
        unique_users = company_user_game_stats.unique_users + 
            CASE 
                WHEN EXISTS (
                    SELECT 1 FROM library l
                    JOIN games_developers gd2 ON l.game_id = gd2.game_id
                    JOIN developer d2 ON d2.id = gd2.developer_id
                    WHERE l.user_id = NEW.user_id
                      AND gd2.game_id != NEW.game_id
                      AND d2.name = company_user_game_stats.company_name
                ) THEN 0
                ELSE 1
            END,
        last_updated = NOW();

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_trigger WHERE tgname = 'trg_update_stats_after_insert'
    ) THEN
        DROP TRIGGER trg_update_stats_after_insert ON library;
    END IF;
END$$;

CREATE TRIGGER trg_update_stats_after_insert
AFTER INSERT ON library
FOR EACH ROW
EXECUTE FUNCTION update_company_game_stats_on_insert();