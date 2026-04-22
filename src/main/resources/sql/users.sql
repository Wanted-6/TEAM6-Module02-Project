-- 사용자 생성
CREATE USER 'meat4'@'%' IDENTIFIED BY '1234';

-- 권한 부여
GRANT ALL PRIVILEGES ON meatdb.* TO 'meat4'@'%';

-- 적용
FLUSH PRIVILEGES;