# --- !Ups
--CREATE TABLE "entries" (
--  "id" SERIAL NOT NULL PRIMARY KEY,
--  "amount" DOUBLE PRECISION NOT NULL,
--  "description" TEXT,
--  "entry_time" TIMESTAMP NOT NULL,
--  "cat_id" INTEGER
--);

--CREATE TABLE "categories" (
--  "id" SERIAL NOT NULL PRIMARY KEY,
--  "name" VARCHAR NOT NULL UNIQUE,
--  "description" TEXT,
--);

create table "entries" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"amount" DOUBLE PRECISION NOT NULL,"description" VARCHAR NOT NULL,"entry_time" TIMESTAMP NOT NULL,"cat_id" BIGINT DEFAULT 0 NOT NULL)
create table "categories" ("id" BIGSERIAL NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL,"description" VARCHAR NOT NULL);

alter table "entries" add constraint "cat_fk" foreign key("cat_id") references "categories"("id") on update NO ACTION on delete SET NULL;



INSERT INTO "categories" VALUES (0, ' None ', 'Default Category');

# --- !Downs
--DROP TABLE "entries";
--DROP TABLE "categories";
alter table "entries" drop constraint "cat_fk";
drop table "entries";
drop table "categories";