-- GetGamesByTitle

CREATE INDEX idx_game_name_fts ON game USING GIN (to_tsvector('english', name));

