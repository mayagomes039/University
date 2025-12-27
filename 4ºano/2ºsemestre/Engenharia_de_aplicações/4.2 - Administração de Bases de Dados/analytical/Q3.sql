-- arguments:
--   name prefix (Ubisoft)
SELECT count(*) AS total, count(DISTINCT id) AS unique
FROM (
    (
        SELECT p.name, u.id
        FROM publisher p
        JOIN games_publishers gp ON gp.publisher_id = p.id
        JOIN game g ON g.id = gp.game_id
        JOIN library l ON l.game_id = g.id
        JOIN users u ON u.id = l.user_id
    )
    UNION ALL
    (
        SELECT d.name, u.id
        FROM developer d
        JOIN games_developers gd ON gd.developer_id = d.id
        JOIN game g ON g.id = gd.game_id
        JOIN library l ON l.game_id = g.id
        JOIN users u ON u.id = l.user_id 
    )
)
WHERE name LIKE 'Ubisoft%';

-- Rewritten Query

WITH publisher_user_games AS (
    SELECT l.user_id, l.game_id
    FROM games_publishers gp
    JOIN publisher p ON p.id = gp.publisher_id
    JOIN library l ON l.game_id = gp.game_id
    WHERE p.name LIKE 'Ubisoft'
),
developer_user_games AS (
    SELECT l.user_id, l.game_id
    FROM games_developers gd
    JOIN developer d ON d.id = gd.developer_id
    JOIN library l ON l.game_id = gd.game_id
    WHERE d.name LIKE 'Ubisoft'
),
combined_user_games AS (
    SELECT user_id, game_id FROM publisher_user_games
    UNION ALL
    SELECT user_id, game_id FROM developer_user_games
)
SELECT
    COUNT(*) AS total,
    COUNT(DISTINCT user_id) AS unique
FROM combined_user_games;