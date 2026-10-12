import logging

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from . import llm
from .config import settings
from .retriever import Retriever
from .schemas import (
    ChatRequest,
    ChatResponse,
    HealthResponse,
    RootCauseRequest,
    RootCauseResponse,
    Source,
)

logging.basicConfig(level=logging.INFO)

app = FastAPI(title="smart-factory-ai")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

retriever = Retriever()

SYS_PROMPT = (
    "你是智能工厂(MES/WMS/QMS)的智能助手。请严格依据“已知资料”回答：\n"
    "1. 只使用资料中的事实，不臆造流程、参数或结论；\n"
    "2. 涉及操作步骤时分条列出，条理清晰；\n"
    "3. 回答末尾用“参考来源：”列出引用到的资料文件名；\n"
    "4. 资料不足以回答时，明确说明“知识库中未找到依据，建议转人工/联系系统管理员”，不要编造。"
)

ROOT_PROMPT = (
    "你是资深质量工程师，擅长 8D、5Why 与鱼骨图分析。请基于“已知资料”和缺陷信息：\n"
    "1. 给出可能根本原因，按可能性排序，并区分 人/机/料/法/环/测；\n"
    "2. 给出纠正措施与预防措施建议；\n"
    "3. 关键结论后用[来源:文件名]标注；\n"
    "4. 资料不足处标注“需现场验证”，不得编造数据。"
)


def sources_of(hits):
    return [Source(name=h["source"], score=h["score"]) for h in hits]


def context_of(hits):
    return "\n\n".join(f"[来源:{h['source']}]\n{h['text']}" for h in hits)


@app.get("/ai/health", response_model=HealthResponse)
def health():
    return HealthResponse(
        status="UP", llm_enabled=llm.enabled(), documents=len(retriever.chunks)
    )


@app.post("/ai/chat", response_model=ChatResponse)
async def chat(req: ChatRequest):
    hits = retriever.search(req.message, settings.TOP_K)
    messages = [
        {"role": "system", "content": SYS_PROMPT},
        {"role": "user", "content": f"已知资料：\n{context_of(hits)}\n\n用户问题：{req.message}"},
    ]
    answer = await llm.chat(messages)
    if answer is None:
        answer = mock_chat(hits)
    return ChatResponse(
        answer=answer, sources=sources_of(hits), llm_enabled=llm.enabled()
    )


@app.post("/ai/root-cause", response_model=RootCauseResponse)
async def root_cause(req: RootCauseRequest):
    query = " ".join(x for x in [req.defect, req.product, req.process, req.extra] if x)
    hits = retriever.search(query, settings.TOP_K)
    info = (
        f"缺陷现象：{req.defect}\n产品：{req.product or '未知'}\n"
        f"工序：{req.process or '未知'}\n批次：{req.batch_no or '未知'}\n"
        f"补充信息：{req.extra or '无'}"
    )
    messages = [
        {"role": "system", "content": ROOT_PROMPT},
        {"role": "user", "content": f"已知资料：\n{context_of(hits)}\n\n{info}"},
    ]
    analysis = await llm.chat(messages, temperature=0.4)
    causes = []
    if analysis is None:
        analysis, causes = mock_root_cause(req, hits)
    return RootCauseResponse(
        analysis=analysis,
        possible_causes=causes,
        sources=sources_of(hits),
        llm_enabled=llm.enabled(),
    )


def mock_chat(hits):
    if not hits:
        return (
            "【离线演示模式·未配置大模型API】知识库中未检索到相关内容，建议转人工或联系"
            "系统管理员补充资料。配置 LLM_API_KEY 后可获得完整智能问答。"
        )
    lines = ["【离线演示模式·未配置大模型API】根据知识库检索到以下参考："]
    for h in hits:
        snippet = h["text"].replace("\n", " ")[:110]
        lines.append(f"· {snippet}…（来源：{h['source']}）")
    lines.append("配置 LLM_API_KEY 后可获得完整智能问答。")
    return "\n".join(lines)


def mock_root_cause(req, hits):
    causes = [
        "来料/物料批次波动（料）——需核对该批次 IQC 记录",
        "设备参数漂移或工装磨损（机）——需点检与 SPC 数据验证",
        "作业方法未被遵守或 SOP 不清晰（法/人）——需现场观察",
    ]
    head = (
        f"【离线演示模式·未配置大模型API】针对“{req.defect}”，按 8D/鱼骨图给出初步分析：\n"
        "可能根本原因（需现场验证）：\n"
    )
    body = "\n".join(f"{i + 1}. {c}" for i, c in enumerate(causes))
    tail = "\n建议：立即隔离相关批次 → 建 NCR → MRB 评审 → 执行 CAPA → 验证关闭。\n参考资料：\n"
    refs = "\n".join(f"· {h['source']}" for h in hits) or "·（未检索到相关 SOP）"
    text = head + body + tail + refs + "\n配置 LLM_API_KEY 后可获得大模型深度分析。"
    return text, causes
