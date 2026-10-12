<template>
  <el-card class="chat-card">
    <template #header>
      <div class="chat-header">
        <el-icon><ChatDotRound /></el-icon>
        智能工厂知识助手（RAG）
        <el-tag size="small" :type="llmEnabled ? 'success' : 'info'" style="margin-left: 8px">
          {{ llmEnabled ? '大模型已启用' : '离线 Mock 模式' }}
        </el-tag>
      </div>
    </template>

    <div ref="msgBox" class="msg-box">
      <div v-for="(m, i) in messages" :key="i" :class="['msg-row', m.role]">
        <div class="bubble">
          <div style="white-space: pre-wrap">{{ m.content }}</div>
          <div v-if="m.sources?.length" class="src">
            参考来源：
            <el-tag v-for="s in m.sources" :key="s.name" size="small" style="margin-right: 4px">
              {{ s.name }} ({{ s.score }})
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="input-bar">
      <el-input v-model="input" placeholder="请输入问题，如：收货后不合格如何处理？" @keyup.enter="send" />
      <el-button type="primary" @click="send">发送</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import request from '../../utils/request'

const messages = ref<any[]>([])
const input = ref('')
const msgBox = ref<HTMLElement>()
const llmEnabled = ref(false)

onMounted(async () => {
  const h: any = await request.get('/ai/health')
  llmEnabled.value = h.llm_enabled
  messages.value.push({
    role: 'assistant',
    content: '您好，我是智能工厂知识助手，可解答收货、检验、工单、SPC、出库等流程问题，并标注资料来源。'
  })
})

async function send() {
  const q = input.value.trim()
  if (!q) return
  messages.value.push({ role: 'user', content: q })
  input.value = ''
  await scroll()
  const data: any = await request.post('/ai/chat', { message: q })
  messages.value.push({ role: 'assistant', content: data.answer, sources: data.sources })
  scroll()
}

async function scroll() {
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}
</script>

<style scoped>
.chat-card { display: flex; flex-direction: column; height: calc(100vh - 110px) }
.chat-header { display: flex; align-items: center; gap: 6px }
.msg-box { flex: 1; overflow-y: auto; padding: 8px; background: #f5f7fa; border-radius: 4px }
.msg-row { display: flex; margin-bottom: 12px }
.msg-row.user { justify-content: flex-end }
.bubble { max-width: 75%; padding: 10px 12px; border-radius: 8px; background: #fff; line-height: 1.6 }
.msg-row.user .bubble { background: #ecf5ff }
.src { margin-top: 8px; border-top: 1px dashed #dcdfe6; padding-top: 6px; font-size: 12px; color: #909399 }
.input-bar { display: flex; gap: 8px; margin-top: 10px }
</style>
