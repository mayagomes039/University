-- GetRecentGamesPerTag

CREATE INDEX idx_game_release_date ON game(release_date DESC);

CREATE INDEX idx_games_tags_tag_id_game_id  ON games_tags(tag_id, game_id);

