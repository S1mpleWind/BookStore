-- 数据库升级脚本：为现有数据库添加新字段
-- 适用于 bookstore 数据库已存在且有数据的情况

USE bookstore;

-- 1. book 表增加 inventory、publisher、isbn 字段
ALTER TABLE book
    ADD COLUMN inventory INT DEFAULT 100 NULL AFTER sales,
    ADD COLUMN publisher VARCHAR(255) NULL AFTER inventory,
    ADD COLUMN isbn VARCHAR(255) NULL AFTER publisher;

-- 2. user 表增加 email 字段
ALTER TABLE user
    ADD COLUMN email VARCHAR(255) NULL AFTER balance;

-- 3. user_auth 表增加 enable 字段
ALTER TABLE user_auth
    ADD COLUMN enable TINYINT(1) DEFAULT 1 NULL AFTER user_id;

-- 4. 确保 admin 账户是启用状态
UPDATE user_auth SET enable = 1 WHERE username = 'admin';