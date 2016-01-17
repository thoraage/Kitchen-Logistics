--drop index user_email_idx;
--drop index user_item_group_idx;
--drop index global_product_idx;
--drop index user_item_user_idx;
--drop index global_product_code_idx;
--drop index global_product_name_idx;

create index user_email_idx on "user" (email);
create index user_item_group_idx on user_item_group ("userId", name);
create index global_product_idx on global_product (id);
create index user_item_user_idx on user_item(user_id);
create index global_product_code_idx on global_product (code);
create index global_product_name_idx on global_product using gist (lower(name) gist_trgm_ops);
