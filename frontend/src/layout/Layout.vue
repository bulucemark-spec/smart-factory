<template>
  <el-container style="height: 100%">
    <el-aside width="230px" class="aside">
      <div class="logo">智能工厂平台</div>
      <el-menu :default-active="activeMenu" router background-color="#001529" text-color="#c0c4cc"
        active-text-color="#409eff" :collapse="false">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>首页看板</span></el-menu-item>
        <el-sub-menu index="master">
          <template #title><el-icon><Files /></el-icon><span>基础数据</span></template>
          <el-menu-item index="/master/material">物料管理</el-menu-item>
          <el-menu-item index="/master/supplier">供应商</el-menu-item>
          <el-menu-item index="/master/bom">BOM</el-menu-item>
          <el-menu-item index="/master/routing">工艺路线</el-menu-item>
          <el-menu-item index="/master/work-center">工作中心</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="wms">
          <template #title><el-icon><Box /></el-icon><span>仓储 WMS</span></template>
          <el-menu-item index="/wms/asn">到货预告</el-menu-item>
          <el-menu-item index="/wms/receipt">收货</el-menu-item>
          <el-menu-item index="/wms/inventory">库存查询</el-menu-item>
          <el-menu-item index="/wms/putaway">上架作业</el-menu-item>
          <el-menu-item index="/wms/pick">领料拣货</el-menu-item>
          <el-menu-item index="/wms/outbound">出库管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="mes">
          <template #title><el-icon><Setting /></el-icon><span>生产 MES</span></template>
          <el-menu-item index="/mes/work-order">工单管理</el-menu-item>
          <el-menu-item index="/mes/report">报工作业</el-menu-item>
          <el-menu-item index="/mes/genealogy">产品追溯</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="qms">
          <template #title><el-icon><CircleCheck /></el-icon><span>质量 QMS</span></template>
          <el-menu-item index="/qms/inspection">检验任务</el-menu-item>
          <el-menu-item index="/qms/ncr">不合格品 NCR</el-menu-item>
          <el-menu-item index="/qms/capa">CAPA 管理</el-menu-item>
          <el-menu-item index="/qms/spc">SPC 过程控制</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="ai">
          <template #title><el-icon><ChatDotRound /></el-icon><span>AI 赋能</span></template>
          <el-menu-item index="/ai/assistant">智能助手</el-menu-item>
          <el-menu-item index="/ai/root-cause">根因分析</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">{{ route.meta.title }}</div>
        <el-dropdown @command="onCommand">
          <span class="user">
            <el-icon><UserFilled /></el-icon>
            {{ auth.user?.realName || auth.user?.username || '用户' }}
            <el-tag v-for="r in auth.roles" size="small" type="info" style="margin-left: 6px">{{ r }}</el-tag>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const activeMenu = computed(() => route.path)

function onCommand(cmd: string) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.aside {
  background: #001529;
  overflow-y: auto;
}
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-size: 17px;
  font-weight: bold;
  letter-spacing: 1px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.user {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.main {
  background: #f0f2f5;
  padding: 16px;
}
</style>
