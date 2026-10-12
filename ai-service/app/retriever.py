import glob
import math
import os
import re
from collections import Counter, defaultdict
from typing import Dict, List

KNOWLEDGE_DIR = os.path.join(os.path.dirname(__file__), "knowledge")


def tokenize(text: str) -> List[str]:
    """中英文混合分词：英文/数字按词，中文按单字 + 相邻字 bigram。"""
    tokens = re.findall(r"[a-z0-9]+", text.lower())
    han = re.findall(r"[\u4e00-\u9fff]", text)
    tokens.extend(han)
    for i in range(len(han) - 1):
        tokens.append(han[i] + han[i + 1])
    return tokens


def split_chunks(md: str, source: str, max_len: int = 800) -> List[Dict]:
    lines = md.splitlines()
    raw: List[Dict] = []
    heading = ""
    buf: List[str] = []

    def flush():
        text = "\n".join(buf).strip()
        if text:
            raw.append({"text": (heading + "\n" + text).strip(), "source": source})

    for line in lines:
        if re.match(r"^#{1,3}\s", line):
            flush()
            buf = []
            heading = line.lstrip("# ").strip()
        else:
            buf.append(line)
    flush()

    out: List[Dict] = []
    for c in raw:
        if len(c["text"]) <= max_len:
            out.append(c)
        else:
            cur = ""
            for para in c["text"].split("\n\n"):
                if cur and len(cur) + len(para) > max_len:
                    out.append({"text": cur, "source": source})
                    cur = para
                else:
                    cur = (cur + "\n\n" + para).strip()
            if cur:
                out.append({"text": cur, "source": source})
    return out


class Retriever:
    """轻量 TF-IDF 余弦检索；Retriever 抽象便于二期替换为向量库（Milvus）。"""

    def __init__(self):
        self.chunks: List[Dict] = []
        self.df: Dict[str, int] = defaultdict(int)
        self.doc_vecs: List[Dict[str, float]] = []

        for path in sorted(glob.glob(os.path.join(KNOWLEDGE_DIR, "*.md"))):
            source = os.path.basename(path)
            with open(path, encoding="utf-8") as f:
                self.chunks.extend(split_chunks(f.read(), source))

        doc_tokens = [tokenize(c["text"]) for c in self.chunks]
        for toks in doc_tokens:
            for term in set(toks):
                self.df[term] += 1

        n = len(self.chunks)
        for toks in doc_tokens:
            tf = Counter(toks)
            vec = {}
            for term, count in tf.items():
                idf = math.log((n + 1) / (self.df[term] + 0.5)) + 1
                vec[term] = (count / len(toks)) * idf
            self.doc_vecs.append(vec)

    def search(self, query: str, top_k: int = 4) -> List[Dict]:
        n = len(self.chunks)
        qt = tokenize(query)
        if not qt:
            return []
        qtf = Counter(qt)
        qv = {}
        for term, count in qtf.items():
            idf = math.log((n + 1) / (self.df.get(term, 0) + 0.5)) + 1
            qv[term] = (count / len(qt)) * idf
        qnorm = math.sqrt(sum(v * v for v in qv.values()))

        scored = []
        for i, dv in enumerate(self.doc_vecs):
            dot = sum(qv.get(term, 0) * w for term, w in dv.items())
            dnorm = math.sqrt(sum(w * w for w in dv.values()))
            score = dot / (qnorm * dnorm) if qnorm and dnorm else 0
            scored.append((score, i))
        scored.sort(reverse=True)

        hits = []
        for score, i in scored[:top_k]:
            if score > 0.02:
                hits.append({
                    "text": self.chunks[i]["text"],
                    "source": self.chunks[i]["source"],
                    "score": round(score, 4),
                })
        return hits
