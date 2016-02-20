alter table "global_product" add column languageIso6392 VARCHAR(3) NOT NULL DEFAULT 'nob';
ALTER TABLE "global_product" ALTER COLUMN languageIso6392 DROP DEFAULT;
