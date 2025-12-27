CREATE EXTENSION IF NOT EXISTS vector;

DROP TABLE IF EXISTS games_developers;
DROP TABLE IF EXISTS games_publishers;
DROP TABLE IF EXISTS games_genres;
DROP TABLE IF EXISTS games_categories;
DROP TABLE IF EXISTS games_tags;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS library;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS media;
DROP TABLE IF EXISTS game_search_embedding;
DROP TABLE IF EXISTS query_embedding_sample;
DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS developer;
DROP TABLE IF EXISTS publisher;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS users;

CREATE TABLE game (
  id SERIAL PRIMARY KEY,
  name VARCHAR,
  release_date DATE,
  required_age INTEGER,
  price DECIMAL(10, 2),
  short_description VARCHAR,
  long_description VARCHAR,
  support_url VARCHAR,
  platforms VARCHAR[],
  metacritic_score INTEGER,
  metacritic_url VARCHAR,
  achievements INTEGER,
  languages VARCHAR[]
);

CREATE TABLE developer (
  id SERIAL PRIMARY KEY,
  name VARCHAR
);

CREATE TABLE games_developers (
  game_id INTEGER,
  developer_id INTEGER,
  PRIMARY KEY (game_id, developer_id)
);

CREATE TABLE publisher (
  id SERIAL PRIMARY KEY,
  name VARCHAR
);

CREATE TABLE games_publishers (
  game_id INTEGER,
  publisher_id INTEGER,
  PRIMARY KEY (game_id, publisher_id)
);

CREATE TABLE genre (
  id SERIAL PRIMARY KEY,
  name VARCHAR
);

CREATE TABLE games_genres (
  game_id INTEGER,
  genre_id INTEGER,
  PRIMARY KEY (game_id, genre_id)
);

CREATE TABLE category (
  id SERIAL PRIMARY KEY,
  name VARCHAR
);

CREATE TABLE games_categories (
  game_id INTEGER,
  category_id INTEGER,
  PRIMARY KEY (game_id, category_id)
);

CREATE TABLE tag (
  id SERIAL PRIMARY KEY,
  name VARCHAR
);

CREATE TABLE games_tags (
  game_id INTEGER,
  tag_id INTEGER,
  PRIMARY KEY (game_id, tag_id)
);

CREATE TABLE media (
  id SERIAL PRIMARY KEY,
  game_id INTEGER,
  type VARCHAR,
  url VARCHAR
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR UNIQUE,
  email VARCHAR UNIQUE,
  created_date DATE,
  vac_banned BOOLEAN,
  profile_description VARCHAR,
  country VARCHAR
);

CREATE TABLE library (
  user_id INTEGER,
  game_id INTEGER,
  added_date TIMESTAMP,
  buy_price DECIMAL(10, 2),
  playtime INTEGER,
  achievements INTEGER,
  PRIMARY KEY (user_id, game_id)
);

CREATE TABLE review (
  user_id INTEGER,
  game_id INTEGER,
  created_date TIMESTAMP NOT NULL DEFAULT NOW(),
  recommend BOOLEAN,
  text TEXT,
  PRIMARY KEY (user_id, game_id)
);

CREATE TABLE friendship (
  user_id_1 INTEGER,
  user_id_2 INTEGER,
  PRIMARY KEY (user_id_1, user_id_2)
);

CREATE TABLE game_search_embedding (
  game_id INTEGER PRIMARY KEY,
  embedding vector(1024)
);

CREATE TABLE query_embedding_sample (
  id INTEGER PRIMARY KEY,
  query VARCHAR,
  embedding vector(1024)
);
