SELECT year, id, name, sales
FROM (
    SELECT year, id, name, sales,
        rank() OVER (PARTITION BY year ORDER BY sales DESC) AS rank
    FROM (
        SELECT l.year, g.id, g.name, count(*) AS sales
        FROM game g
        JOIN (
            SELECT extract(year FROM added_date) AS year, game_id
            FROM library
        ) l ON l.game_id = g.id
        WHERE price > 0
        GROUP BY 1, 2, 3
    )
)
WHERE rank = 1
ORDER BY year DESC;

-- Rewritten Query

SELECT year, id, name, sales
FROM (
    SELECT l.added_year AS year, g.id, g.name, count(*) AS sales,
        rank() OVER (PARTITION BY l.added_year ORDER BY count(*) DESC) AS rank
    FROM game g
    JOIN library l ON l.game_id = g.id
    WHERE g.price > 0
    GROUP BY l.added_year, g.id, g.name
) ranked
WHERE rank = 1
ORDER BY year DESC;