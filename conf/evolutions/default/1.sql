# --- !Ups
CREATE TABLE "entries" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "amount" DOUBLE PRECISION NOT NULL,
  "description" TEXT,
  "entry_time" TIMESTAMP NOT NULL,
  "cat_id" INTEGER
);

CREATE TABLE "categories" (
  "id" SERIAL NOT NULL PRIMARY KEY,
  "name" VARCHAR NOT NULL UNIQUE,
  "description" TEXT,
);


# --- !Downs
DROP TABLE "entries";
DROP TABLE "categories";