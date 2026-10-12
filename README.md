# 智能工厂 MES-WMS-QMS 一体化平台

面向离散制造（以 SMT + 整机组装为样板）的智能制造一体化运营系统，覆盖 **生产执行 MES、仓储管理 WMS、质量管理 QMS** 三大核心域，并提供 **AI 知识助手与质量根因分析** 能力。系统以端到端业务闭环为目标：从收货、来料检验、上架，到工单下达、领料、生产报工、完工入库、成品检验，再到出库交付；质量异常自动走 NCR → MRB → CAPA 闭环并形成正反向产品追溯。

## 一、系统组成与端口

| 服务 | 技术栈 | 容器端口 | 说明 |
|---|---|---|---|
| frontend | Vue3 + Vite + Element Plus + Pinia + ECharts | 80（主机 `8088`） | 统一前端入口，nginx 反代 `/api` |
| gateway | Spring Cloud Gateway（WebFlux） | 8080 | 统一入口、JWT 校验、CORS、路由转发 |
| system-service | Spring Boot 3 + MyBatis-Plus | 8081 | 认证、组织/用户/角色、物料/供应商/BOM/工艺/工作中心 |
| wms-service | Spring Boot 3 + MyBatis-Plus + OpenFeign | 8082 | 收货、上架、库存、领料拣货、出库 |
| mes-service | Spring Boot 3 + MyBatis-Plus + OpenFeign | 8083 | 工厂建模、工单状态机、报工、投料与谱系 |
| qms-service | Spring Boot 3 + MyBatis-Plus + OpenFeign | 8084 | IQC/OQC、NCR、CAPA、SPC（控制图/Cpk） |
| ai-service | Python FastAPI | 8000 | RAG 知识助手、质量根因分析 |
| MySQL | MySQL 8.0 | 3306 | 4 个业务库，首启自动建表与种子数据 |
| Redis | Redis 7 | 6379 | 缓存（预留） |
| MinIO | MinIO | 9000 / 9001 | 对象存储（预留，控制台 9001） |

## 二、技术栈

- 后端：Java 17、Spring Boot 3.2.5、Spring Cloud 2023.0.1、Spring Cloud Gateway、OpenFeign、MyBatis-Plus 3.5.5、jjwt 0.12.5
- 前端：Vue 3.4、TypeScript、Vite 5、Element Plus 2.7、Pinia、Vue Router、ECharts
- AI：FastAPI、httpx；RAG 采用轻量 TF-IDF 余弦检索（`Retriever` 抽象，二期可替换为向量库 Milvus）
- 基础设施：MySQL 8、Redis 7、MinIO；Docker Compose 一键编排

> 试点期采用「轻量服务 + 固定服务名直连」：以 Gateway + OpenFeign + 本地事务 + 状态机 + 条件库存扣减保证闭环正确，暂不引入 Nacos / Sentinel / Seata / RocketMQ / Milvus / K8s。触发引入的条件见 `docs/01-架构设计与需求完善.md`。

## 三、快速开始（Docker Compose 一键启动）

前置：已安装 Docker / Docker Compose。

```bash
# 1. 进入项目目录
cd smart-factory

# 2.（可选）配置环境变量，尤其是大模型 API Key
cp .env.example .env
#   编辑 .env；不填 LLM_API_KEY 也能运行，AI 自动降级为离线 Mock

# 3. 构建并启动全部服务（首次构建需要数分钟）
docker compose up -d --build

# 4. 查看启动状态与日志
docker compose ps
docker compose logs -f gateway
```

启动完成后：

- 前端：http://localhost:8088
- API 网关：http://localhost:8080
- MinIO 控制台：http://localhost:9001 （minioadmin / minioadmin）

停止与清理：

```bash
docker compose down            # 停止并移除容器（保留数据卷）
docker compose down -v         # 同时删除数据卷（数据库将重新初始化）
```

## 四、大模型 API 配置（可选）

AI 服务使用 **OpenAI 兼容接口**，在 `.env` 中配置：

```ini
LLM_BASE_URL=https://api.openai.com/v1
LLM_API_KEY=sk-xxxx
LLM_MODEL=gpt-4o-mini
```

- 兼容 OpenAI 官方及任何提供 `/v1/chat/completions` 的兼容网关 / 国内主流兼容服务，按服务商文档填写 `LLM_BASE_URL` 与 `LLM_MODEL`。
- 系统会自动处理 `LLM_BASE_URL` 是否包含 `/v1`。
- **未配置 `LLM_API_KEY` 时自动降级为离线 Mock**：检索与流程引导可用，答案标注为离线演示，不影响其他服务。
- RAG 强制基于内置知识库（`ai-service/app/knowledge/*.md`）回答并附「参考来源」；资料不足时提示转人工，不臆造。

## 五、演示账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 系统管理员 | admin | admin123 |
| 计划员 | planner | planner123 |
| 仓管员 | warehouse | warehouse123 |
| 检验员 | inspector | inspector123 |
| 操作员 | operator | operator123 |

## 六、端到端业务闭环演示路径

### 链路 A：来料收货 → IQC → 上架
1. （仓管）`仓储 WMS - 收货`：选择待收货 ASN，填写物料批号与实收数量，确认收货。
   系统自动在待检库位建立待检库存，并向 QMS 推送 IQC 任务。
2. （检验）`质量 QMS - 检验任务`：打开 IQC 任务，逐项录入实测/判定，提交。
   - 合格：库存转合格并自动生成上架任务；
   - 不合格：库存冻结并自动生成 NCR（进入链路 C）。
3. （仓管）`仓储 WMS - 上架作业`：选择目标库位，执行上架。

### 链路 B：工单 → 领料 → 生产 → 完工 → OQC → 出库
1. （计划）`生产 MES - 工单管理`：创建工单（选成品、BOM、工艺路线、数量），随后「下达」。
   系统按 BOM×计划量向 WMS 推送领料波次。
2. （仓管）`仓储 WMS - 领料拣货`：打开波次查看 FIFO 分配结果，「执行拣货」进行条件库存扣减。
3. （生产）`工单管理`：对工单「线边交接」（回写投料与谱系）→「开工」。
4. （操作）`生产 MES - 报工作业`：按工位/工序录入合格、不良、报废数量。
5. （生产）`工单管理`：「完工」——生成成品批次、回填谱系、WMS 成品入库、QMS 建立 OQC。
6. （检验）完成 OQC：合格则成品放行；不合格则冻结并走 NCR/CAPA。
7. （仓管）`仓储 WMS - 出库管理`：创建出库单，合格成品「发运」。
8. （追溯）`生产 MES - 产品追溯`：输入成品批次正向查询子件，或输入子件批次反向查询成品。

### 链路 C：质量异常 → NCR → MRB → CAPA
1. 检验不合格自动生成 NCR 并冻结库存。
2. `质量 QMS - 不合格品 NCR`：MRB 评审确定处置（退货/让步/返工/报废），系统联动 WMS 处置并自动建立 CAPA。
3. `质量 QMS - CAPA 管理`：填写根本原因、纠正/预防措施、责任人（执行）；质量部「验证」。
4. 验证通过则关闭 CAPA 并联动关闭 NCR；不通过退回重做。

### AI 赋能
- `AI 赋能 - 智能助手`：基于 SOP/8D/SPC 知识库问答并标注来源。
- `AI 赋能 - 根因分析`：输入缺陷现象与上下文，按 8D/鱼骨图给出可能根因与措施建议。

## 七、本地开发

### 后端（需 JDK 17、Maven）
```bash
cd backend
mvn clean install -DskipTests
# 分别启动各服务（依赖本地/容器中的 MySQL，可通过环境变量 MYSQL_HOST 等覆盖）
```

### 前端（需 Node 18+）
```bash
cd frontend
npm install
npm run dev      # http://localhost:5173 ，已配置 /api 代理到 http://localhost:8080
```

### AI 服务（需 Python 3.11+）
```bash
cd ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

## 八、文档与交付物

- `docs/01-架构设计与需求完善.md`：需求 Gap、架构决策、服务边界、领域模型、状态机、闭环时序、API 契约、一致性方案、AI 设计、非功能验证、演进路线
- `docs/02-WBS任务拆分与里程碑.md`：WBS、迭代计划、DoD、验收映射、风险登记、上线检查单
- `db/init/*.sql`：四库 DDL 与种子数据（MySQL 首启自动执行）

## 九、二期演进

视觉检测、预测性维护等 AI 场景强依赖标注样本与设备数据，列为二期（先解决数据采集与标注）；随着规模增长，按架构文档触发条件逐步引入注册配置中心、消息队列、分布式事务、向量库与容器编排平台。
