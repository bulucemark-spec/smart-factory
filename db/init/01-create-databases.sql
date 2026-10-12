SET NAMES utf8mb4;

-- =============================================================
-- 01-create-databases.sql
-- 智能工厂 MES/WMS/QMS 四库初始化（MySQL 8.0，首次启动自动执行）
-- =============================================================
CREATE DATABASE IF NOT EXISTS smart_system
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smart_wms
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smart_mes
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS smart_qms
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 统一使用 utf8mb4
ALTER DATABASE smart_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE smart_wms    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE smart_mes    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE smart_qms    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
