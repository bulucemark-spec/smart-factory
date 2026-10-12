SET NAMES utf8mb4;

-- =============================================================
-- 03-wms.sql  仓储管理（smart_wms）
-- =============================================================
USE smart_wms;

-- ---------- 仓库 ----------
DROP TABLE IF EXISTS wms_warehouse;
CREATE TABLE wms_warehouse (
  id          BIGINT PRIMARY KEY,
  code        VARCHAR(50) NOT NULL,
  name        VARCHAR(100) NOT NULL,
  type        VARCHAR(20) COMMENT 'CENTER/LINE/FG/RETURN',
  address     VARCHAR(200),
  status      TINYINT DEFAULT 1,
  deleted     TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wh_code (code)
) COMMENT '仓库';

-- ---------- 库区 ----------
DROP TABLE IF EXISTS wms_zone;
CREATE TABLE wms_zone (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  warehouse_id BIGINT NOT NULL,
  code         VARCHAR(50),
  name         VARCHAR(100),
  zone_type    VARCHAR(20),
  KEY idx_wh (warehouse_id)
) COMMENT '库区';

-- ---------- 库位 ----------
DROP TABLE IF EXISTS wms_location;
CREATE TABLE wms_location (
  id            BIGINT PRIMARY KEY,
  warehouse_id  BIGINT NOT NULL,
  zone_id       BIGINT,
  aisle         VARCHAR(20),
  code          VARCHAR(50) NOT NULL,
  x             INT DEFAULT 0 COMMENT '坐标:巷道方向',
  y             INT DEFAULT 0 COMMENT '坐标:纵深方向',
  layer         INT DEFAULT 1,
  capacity      DECIMAL(18,2) DEFAULT 1000,
  location_type VARCHAR(20) DEFAULT 'STORAGE' COMMENT 'STORAGE/RECEIVING/SHIPPING',
  status        TINYINT DEFAULT 1 COMMENT '1可用 0停用',
  deleted       TINYINT DEFAULT 0,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_loc_code (code)
) COMMENT '库位';

-- ---------- ASN 预先发货通知/到货预约 ----------
DROP TABLE IF EXISTS wms_asn;
CREATE TABLE wms_asn (
  id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
  asn_no              VARCHAR(50) NOT NULL,
  supplier_id         BIGINT,
  supplier_name       VARCHAR(100),
  warehouse_id        BIGINT,
  expect_arrival_time DATETIME,
  status              VARCHAR(20) DEFAULT 'PLANNED' COMMENT 'PLANNED/RECEIVED/CLOSED',
  remark              VARCHAR(200),
  deleted             TINYINT DEFAULT 0,
  create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_asn_no (asn_no)
) COMMENT '到货预约/ASN';

DROP TABLE IF EXISTS wms_asn_item;
CREATE TABLE wms_asn_item (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  asn_id        BIGINT NOT NULL,
  material_id   BIGINT,
  material_code VARCHAR(50),
  material_name VARCHAR(100),
  plan_qty      DECIMAL(18,4) DEFAULT 0,
  received_qty  DECIMAL(18,4) DEFAULT 0,
  KEY idx_asn (asn_id)
) COMMENT 'ASN明细';

-- ---------- 收货单 ----------
DROP TABLE IF EXISTS wms_receipt;
CREATE TABLE wms_receipt (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  receipt_no    VARCHAR(50) NOT NULL,
  asn_id        BIGINT,
  warehouse_id  BIGINT,
  supplier_id   BIGINT,
  supplier_name VARCHAR(100),
  status        VARCHAR(20) DEFAULT 'DONE' COMMENT 'DRAFT/DONE',
  operator      BIGINT,
  operator_name VARCHAR(50),
  receive_time  DATETIME,
  remark        VARCHAR(200),
  deleted       TINYINT DEFAULT 0,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_receipt_no (receipt_no)
) COMMENT '收货单';

DROP TABLE IF EXISTS wms_receipt_item;
CREATE TABLE wms_receipt_item (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  receipt_id    BIGINT NOT NULL,
  asn_item_id   BIGINT,
  material_id   BIGINT,
  material_code VARCHAR(50),
  material_name VARCHAR(100),
  batch_no      VARCHAR(50),
  production_date DATE,
  qty           DECIMAL(18,4) DEFAULT 0,
  status        VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING待检',
  location_id   BIGINT COMMENT '待检区库位',
  KEY idx_receipt (receipt_id)
) COMMENT '收货明细';

-- ---------- 库存 ----------
DROP TABLE IF EXISTS wms_inventory;
CREATE TABLE wms_inventory (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  warehouse_id    BIGINT NOT NULL,
  location_id     BIGINT NOT NULL,
  material_id     BIGINT NOT NULL,
  material_code   VARCHAR(50),
  material_name   VARCHAR(100),
  batch_no        VARCHAR(50),
  qty             DECIMAL(18,4) DEFAULT 0,
  status          VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/QUALIFIED/FROZEN/SCRAPPED',
  production_date DATE,
  expiry_date     DATE,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_inv (location_id,material_id,batch_no,status)
) COMMENT '库存';

-- ---------- 上架任务 ----------
DROP TABLE IF EXISTS wms_putaway_task;
CREATE TABLE wms_putaway_task (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_no          VARCHAR(50) NOT NULL,
  receipt_item_id  BIGINT,
  warehouse_id     BIGINT,
  material_id      BIGINT,
  material_name    VARCHAR(100),
  batch_no         VARCHAR(50),
  qty              DECIMAL(18,4),
  from_location_id BIGINT,
  to_location_id   BIGINT,
  to_location_code VARCHAR(50),
  status           VARCHAR(20) DEFAULT 'NEW' COMMENT 'NEW/DONE/CANCELLED',
  operator_name    VARCHAR(50),
  finish_time      DATETIME,
  deleted          TINYINT DEFAULT 0,
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pa_no (task_no)
) COMMENT '上架任务';

-- ---------- 波次 / 拣货任务 ----------
DROP TABLE IF EXISTS wms_pick_wave;
CREATE TABLE wms_pick_wave (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  wave_no     VARCHAR(50) NOT NULL,
  source_type VARCHAR(20) COMMENT 'WORK_ORDER/OUTBOUND',
  source_id   BIGINT,
  source_no   VARCHAR(50),
  warehouse_id BIGINT,
  status      VARCHAR(20) DEFAULT 'NEW' COMMENT 'NEW/RELEASED/PICKED/DONE/CANCELLED',
  deleted     TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wave_no (wave_no)
) COMMENT '拣货波次';

DROP TABLE IF EXISTS wms_pick_task;
CREATE TABLE wms_pick_task (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  wave_id       BIGINT NOT NULL,
  material_id   BIGINT,
  material_code VARCHAR(50),
  material_name VARCHAR(100),
  batch_no      VARCHAR(50),
  location_id   BIGINT,
  location_code VARCHAR(50),
  qty           DECIMAL(18,4),
  picked_qty    DECIMAL(18,4) DEFAULT 0,
  status        VARCHAR(20) DEFAULT 'NEW' COMMENT 'NEW/PICKED',
  operator_name VARCHAR(50),
  pick_time     DATETIME,
  KEY idx_wave (wave_id)
) COMMENT '拣货任务';

-- ---------- 出库单 ----------
DROP TABLE IF EXISTS wms_outbound;
CREATE TABLE wms_outbound (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  outbound_no  VARCHAR(50) NOT NULL,
  type         VARCHAR(20) DEFAULT 'SALES' COMMENT 'SALES/TRANSFER/RETURN/SCRAP',
  customer     VARCHAR(100),
  warehouse_id BIGINT,
  status       VARCHAR(20) DEFAULT 'NEW' COMMENT 'NEW/PICKED/CHECKED/SHIPPED/CANCELLED',
  remark       VARCHAR(200),
  operator_name VARCHAR(50),
  ship_time    DATETIME,
  deleted      TINYINT DEFAULT 0,
  create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ob_no (outbound_no)
) COMMENT '出库单';

DROP TABLE IF EXISTS wms_outbound_item;
CREATE TABLE wms_outbound_item (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  outbound_id   BIGINT NOT NULL,
  material_id   BIGINT,
  material_code VARCHAR(50),
  material_name VARCHAR(100),
  batch_no      VARCHAR(50),
  qty           DECIMAL(18,4),
  picked_qty    DECIMAL(18,4) DEFAULT 0,
  location_code VARCHAR(50),
  KEY idx_ob (outbound_id)
) COMMENT '出库明细';

-- ---------- 盘点（预留） ----------
DROP TABLE IF EXISTS wms_stock_count;
CREATE TABLE wms_stock_count (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  count_no     VARCHAR(50),
  warehouse_id BIGINT,
  type         VARCHAR(20) COMMENT 'PERIOD/CYCLE/SPOT',
  status       VARCHAR(20) DEFAULT 'NEW',
  create_time  DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '盘点单';

DROP TABLE IF EXISTS wms_stock_count_item;
CREATE TABLE wms_stock_count_item (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  count_id     BIGINT,
  location_id  BIGINT,
  material_id  BIGINT,
  batch_no     VARCHAR(50),
  stock_qty    DECIMAL(18,4),
  counted_qty  DECIMAL(18,4),
  diff_qty     DECIMAL(18,4)
) COMMENT '盘点明细';

-- =============================================================
-- 种子数据：仓库 + 库位
-- =============================================================
INSERT INTO wms_warehouse (id,code,name,type,address) VALUES
 (1,'WH-RAW','原材料中心仓','CENTER','1号厂房1层'),
 (2,'WH-LINE','车间线边仓','LINE','车间内'),
 (3,'WH-FG','成品仓','FG','2号厂房1层'),
 (4,'WH-RTN','退货/不合格仓','RETURN','1号厂房辅楼');

-- 原材料仓：待检区 + 货架
INSERT INTO wms_location (id,warehouse_id,aisle,code,x,y,layer,capacity,location_type) VALUES
 (101,1,'RECV','RECV-01',0,0,1,5000,'RECEIVING'),
 (201,1,'A','A-01-01',1,1,1,500,'STORAGE'),
 (202,1,'A','A-01-02',1,1,2,500,'STORAGE'),
 (203,1,'A','A-02-01',1,2,1,500,'STORAGE'),
 (204,1,'A','A-02-02',1,2,2,500,'STORAGE'),
 (205,1,'B','B-01-01',2,1,1,500,'STORAGE'),
 (206,1,'B','B-01-02',2,1,2,500,'STORAGE');

-- 成品仓：成品货位 + 发货区
INSERT INTO wms_location (id,warehouse_id,aisle,code,x,y,layer,capacity,location_type) VALUES
 (301,3,'F','FG-01-01',1,1,1,300,'STORAGE'),
 (302,3,'F','FG-02-01',1,2,1,300,'STORAGE'),
 (399,3,'SHIP','FG-SHIP',0,0,1,1000,'SHIPPING');

-- 退货仓：不合格暂存
INSERT INTO wms_location (id,warehouse_id,aisle,code,x,y,layer,capacity,location_type) VALUES
 (401,4,'N','NCR-01',1,1,1,300,'STORAGE');

-- 线边仓简单货位
INSERT INTO wms_location (id,warehouse_id,aisle,code,x,y,layer,capacity,location_type) VALUES
 (501,2,'L','LINE-SMT',1,1,1,200,'STORAGE'),
 (502,2,'L','LINE-ASM',1,2,1,200,'STORAGE');
