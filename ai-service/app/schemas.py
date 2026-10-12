from typing import List, Optional

from pydantic import BaseModel, Field


class Source(BaseModel):
    name: str
    score: float


class ChatRequest(BaseModel):
    message: str = Field(..., description="用户问题")
    history: Optional[List[dict]] = None


class ChatResponse(BaseModel):
    answer: str
    sources: List[Source] = []
    llm_enabled: bool = False


class RootCauseRequest(BaseModel):
    defect: str = Field(..., description="缺陷/异常现象")
    product: Optional[str] = None
    process: Optional[str] = None
    batch_no: Optional[str] = None
    extra: Optional[str] = Field(None, description="测量数据/发生频次等补充信息")


class RootCauseResponse(BaseModel):
    analysis: str
    possible_causes: List[str] = []
    sources: List[Source] = []
    llm_enabled: bool = False


class HealthResponse(BaseModel):
    status: str
    llm_enabled: bool
    documents: int
