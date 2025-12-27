-- Q1 Optimizations

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'library' AND column_name = 'added_year'
    ) THEN
        ALTER TABLE library
        ADD COLUMN added_year INT GENERATED ALWAYS AS (EXTRACT(YEAR FROM added_date)) STORED;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_library_year_added ON library (added_year);
CREATE INDEX IF NOT EXISTS idx_library_game_id ON library (game_id);
CREATE INDEX IF NOT EXISTS idx_library_year_game ON library (added_year, game_id);

ALTER TABLE library ALTER COLUMN added_year SET STATISTICS 1000;
ALTER TABLE game ALTER COLUMN id SET STATISTICS 1000;

VACUUM ANALYZE library;
VACUUM ANALYZE game;

CLUSTER library USING idx_library_year_game;
