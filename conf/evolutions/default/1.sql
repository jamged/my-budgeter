# --- !Ups
CREATE TABLE entry (
  id SERIAL NOT NULL PRIMARY KEY,
  amount DOUBLE PRECISION NOT NULL,
  description TEXT,
  entry_time TIMESTAMP NOT NULL
);

--CREATE TABLE tag (
--  id SERIAL NOT NULL PRIMARY KEY,
--  name TEXT NOT NULL,
--  description TEXT,
--  parent_id INTEGER
--);

# --- !Downs
DROP TABLE entry;
--DROP TABLE tag;