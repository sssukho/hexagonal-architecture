CREATE DATABASE IF NOT EXISTS hexagonal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'sssukho'@'%' IDENTIFIED BY 'sssukho1234';

GRANT ALL PRIVILEGES ON hexagonal.* TO 'sssukho'@'%';

FLUSH PRIVILEGES;

USE hexagonal;

CREATE TABLE IF NOT EXISTS room (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  owner_id BIGINT NOT NULL,
  area FLOAT(53),
  address VARCHAR(255),
  description VARCHAR(255),
  room_type enum ('ONE_ROOM','THREE_ROOM','TWO_ROOM') NOT NULL,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  primary key (id)
) engine=InnoDB;
CREATE INDEX idx_room_owner_id ON room(owner_id);
CREATE INDEX idx_room_room_type ON room(room_type); -- 조건 검색시
CREATE INDEX idx_room_created_at_desc ON room (created_at DESC); -- 정렬 조건

CREATE TABLE IF NOT EXISTS deal (
  id BIGINT NOT NULL AUTO_INCREMENT,
  room_id bigint NOT NULL,
  deposit DECIMAL(12,0) NOT NULL,
  monthly_rent DECIMAL(12,0),
  deal_type enum ('MONTHLY_RENT','YEAR_RENT') NOT NULL,
  created_at DATETIME(6),
  PRIMARY KEY (id)
 ) engine=InnoDB;
CREATE INDEX idx_deal_room_id ON deal(room_id);
CREATE INDEX idx_deal_type_deposit_monthly_rent ON deal(deal_type, deposit, monthly_rent); -- 전체 검색시
CREATE INDEX idx_deposit_monthly_rent ON deal(deposit, monthly_rent);  -- 조건 검색시
CREATE INDEX idx_deal_type_monthly_rent ON deal(deal_type, monthly_rent); -- 조건 검색시
CREATE INDEX idx_monthly_rent ON deal(monthly_rent);  -- 조건 검색시


CREATE TABLE IF NOT EXISTS member (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  refresh_token VARCHAR(255),
  created_at DATETIME(6),
  PRIMARY KEY (id)
) engine=InnoDB;
CREATE UNIQUE INDEX idx_member_email ON member(email);
