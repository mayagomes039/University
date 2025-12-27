-- getUserInfo

CREATE INDEX idx_user_profile_summary 
ON users(id, username, created_date, vac_banned, profile_description, country);

-- getUserTopGames

CREATE INDEX idx_covering_user_top_games
ON library(user_id, playtime DESC, game_id);

-- buyGame

CREATE INDEX idx_game_id_include_price ON game(id) INCLUDE (price);

