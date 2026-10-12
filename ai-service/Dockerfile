FROM python:3.11-slim

WORKDIR /app
ENV TZ=Asia/Shanghai \
    PIP_NO_CACHE_DIR=1

COPY requirements.txt .
RUN pip install -i https://mirrors.aliyun.com/pypi/simple -r requirements.txt

COPY app ./app

EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
