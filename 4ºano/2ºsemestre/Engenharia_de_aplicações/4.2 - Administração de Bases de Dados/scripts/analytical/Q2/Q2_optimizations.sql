CREATE INDEX IF NOT EXISTS idx_friendship_user_ids ON friendship (user_id_1, user_id_2);
CREATE INDEX IF NOT EXISTS idx_friendship_user_ids_reversed ON friendship (user_id_2, user_id_1);
CREATE INDEX IF NOT EXISTS idx_library_user_game ON library (user_id, game_id);
CREATE INDEX IF NOT EXISTS idx_library_game_user ON library (game_id, user_id);