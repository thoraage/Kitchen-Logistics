alter table "user_item" add column "updated" timestamp not null default now();
alter table "user_item" add column "amount" real not null default 1.0;
