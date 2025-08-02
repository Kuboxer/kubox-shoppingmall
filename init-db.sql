-- 데이터베이스 초기화 스크립트

-- 각 서비스별 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS product_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS cart_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- shop_user에게 모든 데이터베이스 권한 부여
GRANT ALL PRIVILEGES ON user_db.* TO 'shop_user'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'shop_user'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'shop_user'@'%';
GRANT ALL PRIVILEGES ON payment_db.* TO 'shop_user'@'%';
GRANT ALL PRIVILEGES ON cart_db.* TO 'shop_user'@'%';

FLUSH PRIVILEGES;
