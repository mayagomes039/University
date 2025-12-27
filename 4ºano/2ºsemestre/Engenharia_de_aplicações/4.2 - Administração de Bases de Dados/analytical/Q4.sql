-- arguments:
--   lower date (2020-01-01)
--   bin length (12 hours)
WITH bins AS (
    SELECT generate_series('2003-09-12', now(), '12 hours') AS bin
)
SELECT bin,
    count(*) FILTER (WHERE buy_price > 0) AS paid_copies,
    count(*) FILTER (WHERE buy_price = 0) AS free_copies,
    sum(buy_price) AS money_generated
FROM bins
JOIN library ON bin = date_bin('12 hours', added_date, '2020-01-01')
WHERE bin >= '2020-01-01'
GROUP BY bin
ORDER BY bin;

-- Rewritten version

SELECT binned_added_date AS bin,
       count(*) FILTER (WHERE buy_price > 0) AS paid_copies,
       count(*) FILTER (WHERE buy_price = 0) AS free_copies,
       sum(buy_price) AS money_generated
FROM library
WHERE binned_added_date >= '2020-01-01'
GROUP BY binned_added_date
ORDER BY binned_added_date;

