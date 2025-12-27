DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'library' AND column_name = 'playtime_hours'
    ) THEN
        ALTER TABLE library 
        ADD COLUMN playtime_hours INT GENERATED ALWAYS AS ((playtime / 60)::int) STORED;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_users_id_vac ON users(id, vac_banned);
CREATE INDEX IF NOT EXISTS idx_library_game_playtime_date 
ON library(game_id, playtime DESC, added_date DESC);
