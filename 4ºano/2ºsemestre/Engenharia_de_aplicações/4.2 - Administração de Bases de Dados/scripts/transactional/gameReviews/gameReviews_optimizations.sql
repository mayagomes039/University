-- getGamesRecentReviews
-- getGamesScores

CREATE INDEX idx_review_game_date_recommend ON review(game_id, created_date DESC) INCLUDE (recommend, text);

