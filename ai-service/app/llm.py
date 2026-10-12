import logging
from typing import List, Optional

import httpx

from .config import settings

logger = logging.getLogger("ai-service")


def _endpoint() -> str:
    base = settings.LLM_BASE_URL.rstrip("/")
    if not base.endswith("/v1"):
        base += "/v1"
    return base + "/chat/completions"


def enabled() -> bool:
    return bool(settings.LLM_API_KEY)


async def chat(messages: List[dict], temperature: float = 0.3) -> Optional[str]:
    """调用 OpenAI 兼容 /chat/completions；未配置 key 返回 None 触发离线 Mock。"""
    if not enabled():
        return None

    headers = {
        "Authorization": f"Bearer {settings.LLM_API_KEY}",
        "Content-Type": "application/json",
    }
    payload = {
        "model": settings.LLM_MODEL,
        "messages": messages,
        "temperature": temperature,
    }
    try:
        async with httpx.AsyncClient(timeout=settings.REQUEST_TIMEOUT) as client:
            resp = await client.post(_endpoint(), headers=headers, json=payload)
            resp.raise_for_status()
            data = resp.json()
            return data["choices"][0]["message"]["content"]
    except Exception as exc:  # 调用失败不致命，降级为 Mock
        logger.warning("LLM 调用失败，降级 Mock: %s", exc)
        return None
