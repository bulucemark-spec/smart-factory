<template>
  <div class="login-bg">
    <el-card class="login-card">
      <div class="brand">智能工厂 MES-WMS-QMS 平台</div>
      <div class="sub">智能制造一体化运营系统</div>
      <el-form :model="form" @keyup.enter="onLogin">
        <el-form-item>
          <el-input v-model="form.username" size="large" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" size="large" type="password" placeholder="密码"
            :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="onLogin">
          登 录
        </el-button>
      </el-form>
      <div class="tips">
        演示账号：admin/admin123、planner/planner123<br />
        warehouse/warehouse123、inspector/inspector123、operator/operator123
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

async function onLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-bg {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}
.login-card {
  width: 420px;
  padding: 20px 16px;
  border-radius: 10px;
}
.brand {
  text-align: center;
  font-size: 22px;
  font-weight: bold;
  color: #1e3c72;
}
.sub {
  text-align: center;
  color: #909399;
  margin: 8px 0 24px;
  font-size: 13px;
}
.tips {
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
  text-align: center;
}
</style>
