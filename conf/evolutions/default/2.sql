# --- !Ups

alter table "people"
  add "sex" varchar  default '' not null;

# --- !Downs

alter table "people"
  drop column "sex";
