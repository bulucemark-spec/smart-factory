SET NAMES utf8mb4;

-- =============================================================
-- 04-mes.sql  制造执行（smart_mes）
-- =============================================================
USE smart_mes;

-- ---------- 工位 ----------
DROP TABLE IF EXISTS mes_workstation;
CREATE TABLE mes_workstation (
  id               BIGINT PRIMARY KEY,
  code             VARCHAR(50) NOT NULL,
  name             VARCHAR(100),
  work_center_id   BIGINT,
  work_center_code VARCHAR(50),
  production_line  VARCHAR(50),
  status           TINYINT DEFAULT 1,
  deleted          TINYINT DEFAULT 0,
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ws_code (code)
) COMMENT '工位';

-- ---------- 工单 ----------
DROP TABLE IF EXISTS mes_work_order;
CREATE TABLE mes_work_order (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no          VARCHAR(50) NOT NULL,
  type              VARCHAR(20) DEFAULT 'PRODUCTION' COMMENT 'PRODUCTION/REWORK/TRIAL/OUTSOURCE',
  product_material_id BIGINT,
  product_code      VARCHAR(50),
  product_name      VARCHAR(100),
  bom_id            BIGINT,
  routing_id        BIGINT,
  work_center_id    BIGINT,
  qty_planned       DECIMAL(18,4),
  qty_completed     DECIMAL(18,4) DEFAULT 0,
  qty_defect        DECIMAL(18,4) DEFAULT 0,
  qty_scrap         DECIMAL(18,4) DEFAULT 0,
  status            VARCHAR(20) DEFAULT 'CREATED'
                    COMMENT 'CREATED/RELEASED/PICKED/STARTED/RUNNING/COMPLETED/STOCKED/CLOSED/CANCELLED',
  finished_batch    VARCHAR(50) COMMENT '完工成品批次',
  plan_start        DATETIME,
  plan_end          DATETIME,
  actual_start      DATETIME,
  actual_end        DATETIME,
  remark            VARCHAR(200),
  deleted           TINYINT DEFAULT 0,
  create_time       DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_no (order_no)
) COMMENT '工单';

-- ---------- 报工记录 ----------
DROP TABLE IF EXISTS mes_work_report;
CREATE TABLE mes_work_report (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_order_id    BIGINT NOT NULL,
  order_no         VARCHAR(50),
  operation_seq    INT,
  operation_name   VARCHAR(100),
  workstation_id   BIGINT,
  workstation_code VARCHAR(50),
  user_id          BIGINT,
  user_name        VARCHAR(50),
  good_qty         DECIMAL(18,4) DEFAULT 0,
  defect_qty       DECIMAL(18,4) DEFAULT 0,
  scrap_qty        DECIMAL(18,4) DEFAULT 0,
  defect_code      VARCHAR(50),
  report_time      DATETIME,
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_wo (work_order_id)
) COMMENT '报工记录';

-- ---------- 投料/退料 ----------
DROP TABLE IF EXISTS mes_material_txn;
CREATE TABLE mes_material_txn (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_order_id BIGINT NOT NULL,
  order_no      VARCHAR(50),
  material_id   BIGINT,
  material_code VARCHAR(50),
  material_name VARCHAR(100),
  batch_no      VARCHAR(50),
  qty           DECIMAL(18,4),
  type          VARCHAR(20) COMMENT 'ISSUE/RETURN',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_wo (work_order_id)
) COMMENT '物料领退';

-- ---------- 批次谱系 ----------
DROP TABLE IF EXISTS mes_genealogy;
CREATE TABLE mes_genealogy (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_order_id BIGINT,
  parent_batch  VARCHAR(50) COMMENT '成品批次',
  child_batch   VARCHAR(50) COMMENT '物料批次',
  material_id   BIGINT,
  material_code VARCHAR(50),
  qty           DECIMAL(18,4),
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_parent (parent_batch),
  KEY idx_child  (child_batch),
  KEY idx_wo (work_order_id)
) COMMENT '批次谱系';

-- ---------- Andon 异常 ----------
DROP TABLE IF EXISTS mes_andon;
CREATE TABLE mes_andon (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  call_no        VARCHAR(50),
  workstation_id BIGINT,
  type           VARCHAR(20) COMMENT 'MATERIAL/EQUIPMENT/QUALITY/TECH',
  level          VARCHAR(20) DEFAULT 'L1',
  status         VARCHAR(20) DEFAULT 'CALL' COMMENT 'CALL/RESPONDING/CLOSED',
  caller_name    VARCHAR(50),
  responder_name VARCHAR(50),
  description    VARCHAR(300),
  call_time      DATETIME,
  respond_time   DATETIME,
  close_time     DATETIME,
  create_time    DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Andon安灯';

-- =============================================================
-- 种子数据：工位
-- =============================================================
INSERT INTO mes_workstation (id,code,name,work_center_id,work_center_code,production_line) VALUES
 (1,'WS-SMT-01','SMT贴片工位1',4001,'WC-SMT','SMT一线'),
 (2,'WS-ASM-01','组装工位1',4002,'WC-ASM','组装一线'),
 (3,'WS-TEST-01','功能测试工位1',4003,'WC-TEST','组装一线'),
 (4,'WS-PKG-01','包装工位1',4004,'WC-PKG','组装一线');
