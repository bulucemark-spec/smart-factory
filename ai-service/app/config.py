import os


class Settings:
    # OpenAI 兼容接口：可指向 OpenAI / Azure 兼容网关 / 国内主流兼容服务
    LLM_BASE_URL = os.getenv("LLM_BASE_URL", "https://api.openai.com/v1")
    LLM_API_KEY = os.getenv("LLM_API_KEY", "")
    LLM_MODEL = os.getenv("LLM_MODEL", "gpt-4o-mini")
    REQUEST_TIMEOUT = float(os.getenv("LLM_TIMEOUT", "60"))
    TOP_K = int(os.getenv("RAG_TOP_K", "4"))


settings = Settings()
