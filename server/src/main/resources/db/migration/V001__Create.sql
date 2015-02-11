create table "global_product" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "code" VARCHAR(254) NOT NULL,
    "name" VARCHAR(254) NOT NULL,
    "created" DATE NOT NULL
);

create table "user" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "username" VARCHAR(254) NOT NULL,
    "mail" VARCHAR(254) NOT NULL,
    "password" BYTEA NOT NULL,
    "created" DATE NOT NULL
);

create table "user_item_group" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "userId" INTEGER NOT NULL,
    "name" VARCHAR(254) NOT NULL,
    "created" DATE NOT NULL
);

alter table "user_item_group"
    add constraint "item_group_user_fk"
    foreign key("userId")
    references "user"("id")
    on update NO ACTION
    on delete NO ACTION;

create table "user_item" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "user_id" INTEGER NOT NULL,
    "product_id" INTEGER NOT NULL,
    "item_group_id" INTEGER NOT NULL,
    "created" DATE NOT NULL
);

alter table "user_item"
    add constraint "item_item_group_fk"
    foreign key("item_group_id")
    references "user_item_group"("id")
    on update NO ACTION
    on delete NO ACTION;

alter table "user_item"
    add constraint "item_user_fk"
    foreign key("user_id")
    references "user"("id")
    on update NO ACTION
    on delete NO ACTION;

alter table "user_item"
    add constraint "item_product_fk"
    foreign key("product_id")
    references "global_product"("id")
    on update NO ACTION
    on delete NO ACTION;
