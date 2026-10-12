SET NAMES utf8mb4;

-- =============================================================
-- 02-system.sql  基础平台 + 主数据（smart_system）
-- 注意：演示用户由 system-service 首次启动时用 BCrypt 自动初始化
-- =============================================================
USE smart_system;

-- ---------- 组织架构（集团-工厂-车间-产线-班组） ----------
DROP TABLE IF EXISTS sys_org;
CREATE TABLE sys_org (
  id          BIGINT PRIMARY KEY,
  parent_id   BIGINT DEFAULT 0,
  org_type    VARCHAR(20) NOT NULL COMMENT 'GROUP/FACTORY/WORKSHOP/LINE/TEAM',
  code        VARCHAR(50),
  name        VARCHAR(100) NOT NULL,
  path        VARCHAR(500) DEFAULT '/',
  sort        INT DEFAULT 0,
  status      TINYINT DEFAULT 1,
  deleted     TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '组织架构';

-- ---------- 用户 ----------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id              BIGINT PRIMARY KEY,
  org_id          BIGINT,
  username        VARCHAR(50) NOT NULL,
  password        VARCHAR(100),
  real_name       VARCHAR(50),
  phone           VARCHAR(20),
  email           VARCHAR(100),
  status          TINYINT DEFAULT 1 COMMENT '1启用 0停用',
  last_login_time DATETIME,
  deleted         TINYINT DEFAULT 0,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) COMMENT '用户';

-- ---------- 角色 ----------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id          BIGINT PRIMARY KEY,
  code        VARCHAR(50) NOT NULL,
  name        VARCHAR(50) NOT NULL,
  remark      VARCHAR(200),
  deleted     TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_code (code)
) COMMENT '角色';

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联';

-- ---------- 权限（菜单/按钮，演示用） ----------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
  id        BIGINT PRIMARY KEY,
  parent_id BIGINT DEFAULT 0,
  name      VARCHAR(50),
  code      VARCHAR(80),
  type      VARCHAR(20) COMMENT 'MENU/BUTTON',
  path      VARCHAR(100),
  icon      VARCHAR(50),
  sort      INT DEFAULT 0
) COMMENT '权限';

DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id)
) COMMENT '角色权限关联';

-- ---------- 物料主数据 ----------
DROP TABLE IF EXISTS mdm_material;
CREATE TABLE mdm_material (
  id              BIGINT PRIMARY KEY,
  code            VARCHAR(50) NOT NULL,
  name            VARCHAR(100) NOT NULL,
  category        VARCHAR(50) COMMENT '成品/半成品/原材料/包材',
  specification   VARCHAR(200),
  unit            VARCHAR(20) DEFAULT '个',
  material_type   VARCHAR(20) DEFAULT 'FINISHED' COMMENT 'FINISHED/PRODUCT/RAW/PACKAGE',
  batch_managed   TINYINT DEFAULT 1,
  shelf_life_days INT DEFAULT 0,
  abc_class       VARCHAR(4) DEFAULT 'B',
  weight          DECIMAL(18,4) DEFAULT 0,
  status          TINYINT DEFAULT 1,
  deleted         TINYINT DEFAULT 0,
  create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_material_code (code)
) COMMENT '物料主数据';

-- ---------- 供应商 ----------
DROP TABLE IF EXISTS mdm_supplier;
CREATE TABLE mdm_supplier (
  id             BIGINT PRIMARY KEY,
  code           VARCHAR(50) NOT NULL,
  name           VARCHAR(100) NOT NULL,
  contact_person VARCHAR(50),
  phone          VARCHAR(20),
  address        VARCHAR(200),
  grade          VARCHAR(4) DEFAULT 'B' COMMENT 'A/B/C/D',
  qualified_rate DECIMAL(5,2) DEFAULT 100.00,
  ppm            INT DEFAULT 0,
  status         TINYINT DEFAULT 1,
  deleted        TINYINT DEFAULT 0,
  create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_supplier_code (code)
) COMMENT '供应商';

-- ---------- BOM ----------
DROP TABLE IF EXISTS mdm_bom;
CREATE TABLE mdm_bom (
  id                  BIGINT PRIMARY KEY,
  code                VARCHAR(50),
  product_material_id BIGINT NOT NULL,
  version             VARCHAR(20) DEFAULT 'V1.0',
  status              TINYINT DEFAULT 1,
  remark              VARCHAR(200),
  deleted             TINYINT DEFAULT 0,
  create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'BOM 主表';

DROP TABLE IF EXISTS mdm_bom_item;
CREATE TABLE mdm_bom_item (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  bom_id      BIGINT NOT NULL,
  material_id BIGINT NOT NULL,
  quantity    DECIMAL(18,4) NOT NULL DEFAULT 1,
  unit        VARCHAR(20),
  sequence    INT DEFAULT 10,
  loss_rate   DECIMAL(6,2) DEFAULT 0,
  KEY idx_bom (bom_id)
) COMMENT 'BOM 明细';

-- ---------- 工作中心 ----------
DROP TABLE IF EXISTS mdm_work_center;
CREATE TABLE mdm_work_center (
  id               BIGINT PRIMARY KEY,
  code             VARCHAR(50) NOT NULL,
  name             VARCHAR(100) NOT NULL,
  workshop         VARCHAR(100),
  org_id           BIGINT,
  capacity_per_day DECIMAL(18,2) DEFAULT 800,
  status           TINYINT DEFAULT 1,
  deleted          TINYINT DEFAULT 0,
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wc_code (code)
) COMMENT '工作中心';

-- ---------- 工艺路线 ----------
DROP TABLE IF EXISTS mdm_routing;
CREATE TABLE mdm_routing (
  id                  BIGINT PRIMARY KEY,
  code                VARCHAR(50),
  product_material_id BIGINT NOT NULL,
  version             VARCHAR(20) DEFAULT 'V1.0',
  status              TINYINT DEFAULT 1,
  remark              VARCHAR(200),
  deleted             TINYINT DEFAULT 0,
  create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '工艺路线主表';

DROP TABLE IF EXISTS mdm_routing_operation;
CREATE TABLE mdm_routing_operation (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  routing_id     BIGINT NOT NULL,
  sequence       INT NOT NULL,
  operation_name VARCHAR(100) NOT NULL,
  work_center_id BIGINT,
  standard_time  DECIMAL(10,2) DEFAULT 0 COMMENT '标准工时(分钟)',
  is_qc_point    TINYINT DEFAULT 0,
  ctq            TINYINT DEFAULT 0,
  description    VARCHAR(200),
  KEY idx_routing (routing_id)
) COMMENT '工序';

-- =============================================================
-- 种子数据
-- =============================================================
-- 组织
INSERT INTO sys_org (id,parent_id,org_type,code,name,path,sort) VALUES
 (1,0,'GROUP','G01','智造科技集团','/1/',1),
 (2,1,'FACTORY','F01','上海一号工厂','/1/2/',1),
 (3,2,'WORKSHOP','W01','SMT车间','/1/2/3/',1),
 (4,2,'WORKSHOP','W02','组装车间','/1/2/4/',2),
 (5,3,'LINE','L01','SMT一线','/1/2/3/5/',1),
 (6,4,'LINE','L02','组装一线','/1/2/4/6/',1),
 (7,5,'TEAM','T01','SMT一班','/1/2/3/5/7/',1),
 (8,6,'TEAM','T02','组装一班','/1/2/4/6/8/',1);

-- 角色
INSERT INTO sys_role (id,code,name,remark) VALUES
 (1,'ADMIN','系统管理员','全部权限'),
 (2,'PLANNER','计划员','APS排产/工单'),
 (3,'WAREHOUSE','仓管员','仓储作业'),
 (4,'INSPECTOR','质量检验员','检验/NCR'),
 (5,'OPERATOR','一线操作工','报工/Andon');

-- 物料（1001成品；2001+原材料）
INSERT INTO mdm_material
 (id,code,name,category,specification,unit,material_type,batch_managed,shelf_life_days,abc_class,weight) VALUES
 (1001,'FG-100','智能控制器(成品)','成品','SC-100 标准型','台','FINISHED',1,720,'A',0.5000),
 (2001,'RM-PCB-01','PCB主板','原材料','100mm 双层板','块','RAW',1,1095,'A',0.0800),
 (2002,'RM-CASE-01','塑料外壳','原材料','150mm ABS','个','RAW',1,1825,'B',0.1200),
 (2003,'RM-CHIP-01','主控芯片','原材料','QFP-64','颗','RAW',1,1825,'A',0.0050),
 (2004,'RM-SCR-01','十字螺丝','原材料','M3x8','颗','RAW',0,0,'C',0.0010),
 (2005,'PK-BOX-01','包装盒','包材','彩盒 160mm','个','PACKAGE',0,0,'C',0.0300);

-- 供应商
INSERT INTO mdm_supplier
 (id,code,name,contact_person,phone,address,grade,qualified_rate,ppm) VALUES
 (3001,'SUP-001','华鑫电子有限公司','王工','13800000001','深圳市宝安区','A',98.60,350),
 (3002,'SUP-002','恒达塑胶制品厂','李经理','13800000002','苏州市工业园区','B',96.20,1200);

-- 工作中心
INSERT INTO mdm_work_center (id,code,name,workshop,org_id,capacity_per_day) VALUES
 (4001,'WC-SMT','SMT贴片中心','SMT车间',3,1000),
 (4002,'WC-ASM','组装中心','组装车间',4,800),
 (4003,'WC-TEST','功能测试中心','组装车间',4,900),
 (4004,'WC-PKG','包装中心','组装车间',4,1200);

-- BOM（FG-100）
INSERT INTO mdm_bom (id,code,product_material_id,version,remark) VALUES
 (5001,'BOM-FG100',1001,'V1.0','智能控制器标准BOM');
INSERT INTO mdm_bom_item (bom_id,material_id,quantity,unit,sequence) VALUES
 (5001,2001,1,'块',10),
 (5001,2002,1,'个',20),
 (5001,2003,1,'颗',30),
 (5001,2004,4,'颗',40),
 (5001,2005,1,'个',50);

-- 工艺路线（FG-100）
INSERT INTO mdm_routing (id,code,product_material_id,version,remark) VALUES
 (6001,'ROUT-FG100',1001,'V1.0','智能控制器工艺');
INSERT INTO mdm_routing_operation
 (routing_id,sequence,operation_name,work_center_id,standard_time,is_qc_point,ctq,description) VALUES
 (6001,10,'SMT贴片',4001,5.00,1,0,'印刷-贴片-回流焊'),
 (6001,20,'整机组装',4002,8.00,0,0,'装外壳、锁螺丝'),
 (6001,30,'功能测试',4003,3.00,1,1,'通电、通信测试(CTQ)'),
 (6001,40,'包装入库',4004,2.00,0,0,'装盒、贴标');
