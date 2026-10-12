import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
    {
      path: '/',
      component: () => import('../layout/Layout.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页看板' } },
        { path: 'master/material', component: () => import('../views/master/Material.vue'), meta: { title: '物料管理' } },
        { path: 'master/supplier', component: () => import('../views/master/Supplier.vue'), meta: { title: '供应商管理' } },
        { path: 'master/bom', component: () => import('../views/master/Bom.vue'), meta: { title: 'BOM 管理' } },
        { path: 'master/routing', component: () => import('../views/master/Routing.vue'), meta: { title: '工艺路线' } },
        { path: 'master/work-center', component: () => import('../views/master/WorkCenter.vue'), meta: { title: '工作中心' } },
        { path: 'wms/asn', component: () => import('../views/wms/Asn.vue'), meta: { title: '到货预告' } },
        { path: 'wms/receipt', component: () => import('../views/wms/Receipt.vue'), meta: { title: '收货' } },
        { path: 'wms/inventory', component: () => import('../views/wms/Inventory.vue'), meta: { title: '库存查询' } },
        { path: 'wms/putaway', component: () => import('../views/wms/Putaway.vue'), meta: { title: '上架作业' } },
        { path: 'wms/pick', component: () => import('../views/wms/Pick.vue'), meta: { title: '领料拣货' } },
        { path: 'wms/outbound', component: () => import('../views/wms/Outbound.vue'), meta: { title: '出库管理' } },
        { path: 'mes/work-order', component: () => import('../views/mes/WorkOrder.vue'), meta: { title: '工单管理' } },
        { path: 'mes/report', component: () => import('../views/mes/Report.vue'), meta: { title: '报工作业' } },
        { path: 'mes/genealogy', component: () => import('../views/mes/Genealogy.vue'), meta: { title: '产品追溯' } },
        { path: 'qms/inspection', component: () => import('../views/qms/Inspection.vue'), meta: { title: '检验任务' } },
        { path: 'qms/ncr', component: () => import('../views/qms/Ncr.vue'), meta: { title: '不合格品 NCR' } },
        { path: 'qms/capa', component: () => import('../views/qms/Capa.vue'), meta: { title: 'CAPA 管理' } },
        { path: 'qms/spc', component: () => import('../views/qms/Spc.vue'), meta: { title: 'SPC 过程控制' } },
        { path: 'ai/assistant', component: () => import('../views/ai/Assistant.vue'), meta: { title: 'AI 智能助手' } },
        { path: 'ai/root-cause', component: () => import('../views/ai/RootCause.vue'), meta: { title: '质量根因分析' } }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    return '/login'
  }
  if (token && to.path === '/login') {
    return '/'
  }
  return true
})

export default router
