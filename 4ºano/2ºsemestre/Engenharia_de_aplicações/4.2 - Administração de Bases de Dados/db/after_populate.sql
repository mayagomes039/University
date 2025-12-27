SELECT setval('category_id_seq', (SELECT MAX(id) FROM Category));
SELECT setval('developer_id_seq', (SELECT MAX(id) FROM Developer));
SELECT setval('game_id_seq', (SELECT MAX(id) FROM Game));
SELECT setval('genre_id_seq', (SELECT MAX(id) FROM Genre));
SELECT setval('media_id_seq', (SELECT MAX(id) FROM Media));
SELECT setval('publisher_id_seq', (SELECT MAX(id) FROM Publisher));
SELECT setval('tag_id_seq', (SELECT MAX(id) FROM Tag));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM Users));

ALTER SEQUENCE game_id_seq INCREMENT BY 10;
