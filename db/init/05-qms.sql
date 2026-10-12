SET NAMES utf8mb4;

-- =============================================================
-- 05-qms.sql  质量管理（smart_qms）
-- =============================================================
USE smart_qms;

-- ---------- 检验标准（方案） ----------
DROP TABLE IF EXISTS qms_inspection_standard;
CREATE TABLE qms_inspection_standard (
  id             BIGINT PRIMARY KEY,
  code           VARCHAR(50) NOT NULL,
  material_id    BIGINT,
  material_code  VARCHAR(50),
  material_name  VARCHAR(100),
  supplier_id    BIGINT,
  supplier_name  VARCHAR(100),
  inspection_type VARCHAR(20) COMMENT 'IQC/IPQC/OQC',
  aql_level      VARCHAR(10) DEFAULT 'II',
  sample_size    INT DEFAULT 5,
  status         TINYINT DEFAULT 1,
  remark         VARCHAR(200),
  deleted        TINYINT DEFAULT 0,
  create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_std_code (code)
) COMMENT '检验标准';

-- ---------- 检验项目 ----------
DROP TABLE IF EXISTS qms_inspection_item;
CREATE TABLE qms_inspection_item (
  id             BIGINT PRIMARY KEY,
  standard_id    BIGINT NOT NULL,
  name           VARCHAR(100),
  tool           VARCHAR(50),
  standard_value VARCHAR(50),
  upper_limit    DECIMAL(18,4),
  lower_limit    DECIMAL(18,4),
  unit           VARCHAR(20),
  ctq            TINYINT DEFAULT 0,
  sequence       INT DEFAULT 10,
  KEY idx_std (standard_id)
) COMMENT '检验项目';

-- ---------- 检验任务 ----------
DROP TABLE IF EXISTS qms_inspection_task;
CREATE TABLE qms_inspection_task (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_no         VARCHAR(50) NOT NULL,
  inspection_type VARCHAR(20) COMMENT 'IQC/IPQC/OQC',
  source_type     VARCHAR(20),
  source_no       VARCHAR(50),
  material_id     BIGINT,
  material_code   VARCHAR(50),
  material_name   VARCHAR(100),
  batch_no        VARCHAR(50),
  supplier_id     BIGINT,
  supplier_name   VARCHAR(100),
  sample_size     INT DEFAULT 5,
  status          VARCHAR(20) DEFAULT 'PENDING'
                  COMMENT 'PENDING/JUDGING/QUALIFIED/UNQUALIFIED/DISPOSED',
  inspector       BIGINT,
  inspector_name  VARCHAR(50),
  inspect_time    DATETIME,
  conclusion      VARCHAR(300),
  deleted         TINYINT DEFAULT 0,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_task_no (task_no)
) COMMENT '检验任务';

-- ---------- 实测结果 ----------
DROP TABLE IF EXISTS qms_inspection_result;
CREATE TABLE qms_inspection_result (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id        BIGINT NOT NULL,
  item_id        BIGINT,
  item_name      VARCHAR(100),
  measured_value DECIMAL(18,4),
  judge          VARCHAR(10) COMMENT 'OK/NG',
  remark         VARCHAR(200),
  KEY idx_task (task_id)
) COMMENT '检验实测结果';

-- ---------- NCR 不合格报告 ----------
DROP TABLE IF EXISTS qms_ncr;
CREATE TABLE qms_ncr (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  ncr_no        VARCHAR(50) NOT NULL,
  source_type   VARCHAR(20) COMMENT 'IQC/IPQC/OQC/PRODUCTION',
  source_no     VARCHAR(50),
  material_id   BIGINT,
  material_code VARCHAR(50),
  material_name VARCHAR(100),
  batch_no      VARCHAR(50),
  qty           DECIMAL(18,4),
  defect_desc   VARCHAR(500),
  level         VARCHAR(20) DEFAULT 'MAJOR' COMMENT 'CRITICAL/MAJOR/MINOR',
  status        VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN/REVIEWED/IN_PROGRESS/CLOSED',
  disposition   VARCHAR(20) COMMENT 'RETURN/SORT/REWORK/CONCESSION/SCRAP',
  mrb_comment   VARCHAR(500),
  creator_name  VARCHAR(50),
  close_time    DATETIME,
  deleted       TINYINT DEFAULT 0,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ncr_no (ncr_no)
) COMMENT '不合格报告';

-- ---------- CAPA ----------
DROP TABLE IF EXISTS qms_capa;
CREATE TABLE qms_capa (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  capa_no           VARCHAR(50) NOT NULL,
  ncr_id            BIGINT,
  root_cause        VARCHAR(500),
  corrective_action VARCHAR(500),
  preventive_action VARCHAR(500),
  owner             BIGINT,
  owner_name        VARCHAR(50),
  due_date          DATE,
  status            VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN/IN_PROGRESS/VERIFIED/CLOSED',
  verify_result     VARCHAR(300),
  verifier_name     VARCHAR(50),
  create_time       DATETIME DEFAULT CURRENT_TIMESTAMP,
  close_time        DATETIME,
  UNIQUE KEY uk_capa_no (capa_no)
) COMMENT '纠正预防措施';

-- ---------- SPC 记录 ----------
DROP TABLE IF EXISTS qms_spc_record;
CREATE TABLE qms_spc_record (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  characteristic VARCHAR(100),
  material_code VARCHAR(50),
  work_order_no VARCHAR(50),
  value         DECIMAL(18,4),
  source_type   VARCHAR(20),
  record_time   DATETIME,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_char (characteristic,record_time)
) COMMENT 'SPC数据点';

-- ---------- 供应商质量绩效（快照） ----------
DROP TABLE IF EXISTS qms_supplier_quality;
CREATE TABLE qms_supplier_quality (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  supplier_id      BIGINT,
  supplier_name    VARCHAR(100),
  period           VARCHAR(20),
  qualified_rate   DECIMAL(5,2),
  ppm              INT,
  on_time_rate     DECIMAL(5,2),
  cooperation_score DECIMAL(5,2),
  total_score      DECIMAL(5,2),
  grade            VARCHAR(4),
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '供应商质量绩效';

-- =============================================================
-- 种子数据：检验标准 + 项目
-- =============================================================
-- PCB 来料检验
INSERT INTO qms_inspection_standard
 (id,code,material_id,material_code,material_name,supplier_id,supplier_name,inspection_type,aql_level,sample_size,remark) VALUES
 (1,'STD-PCB-IQC',2001,'RM-PCB-01','PCB主板',3001,'华鑫电子有限公司','IQC','II',5,'PCB来料标准'),
 (2,'STD-CASE-IQC',2002,'RM-CASE-01','塑料外壳',3002,'恒达塑胶制品厂','IQC','II',5,'外壳来料标准'),
 (3,'STD-FG-OQC',1001,'FG-100','智能控制器(成品)',NULL,NULL,'OQC','II',3,'成品出货标准');

INSERT INTO qms_inspection_item (id,standard_id,name,tool,standard_value,upper_limit,lower_limit,unit,ctq,sequence) VALUES
 (1,1,'PCB长度','卡尺',NULL,100.2000,99.8000,'mm',1,10),
 (2,1,'PCB厚度','千分尺',NULL,1.7000,1.5000,'mm',0,20),
 (3,1,'外观','目检','无划痕/无虚焊',NULL,NULL,'',0,30),
 (4,2,'外壳长度','卡尺',NULL,150.5000,149.5000,'mm',1,10),
 (5,2,'外观','目检','无飞边/无变色',NULL,NULL,'',0,20),
 (6,3,'功能测试','测试架','PASS',NULL,NULL,'',1,10),
 (7,3,'外观','目检','无划痕',NULL,NULL,'',0,20),
 (8,3,'包装','目检','附件齐全',NULL,NULL,'',0,30);
