import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
      { path: 'search', name: 'Search', component: () => import('../views/Search.vue'), meta: { title: '搜索' } },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('../views/ProductDetail.vue'), meta: { title: '商品详情' } },
      { path: 'publish', name: 'Publish', component: () => import('../views/Publish.vue'), meta: { title: '发布闲置', requireAuth: true } },
      { path: 'user/:id', name: 'UserProfile', component: () => import('../views/UserProfile.vue'), meta: { title: '用户主页' } },
      {
        path: 'my',
        component: () => import('../views/my/MyLayout.vue'),
        meta: { requireAuth: true },
        children: [
          { path: '', name: 'MyCenter', component: () => import('../views/my/MyCenter.vue'), meta: { title: '个人中心' } },
          { path: 'products', name: 'MyProducts', component: () => import('../views/my/MyProducts.vue'), meta: { title: '我的发布' } },
          { path: 'orders', name: 'MyOrders', component: () => import('../views/my/MyOrders.vue'), meta: { title: '我的订单' } },
          { path: 'sold', name: 'MySold', component: () => import('../views/my/MySold.vue'), meta: { title: '卖出的' } },
          { path: 'favorites', name: 'MyFavorites', component: () => import('../views/my/MyFavorites.vue'), meta: { title: '我的收藏' } },
          { path: 'address', name: 'MyAddress', component: () => import('../views/my/MyAddress.vue'), meta: { title: '收货地址' } },
          { path: 'settings', name: 'MySettings', component: () => import('../views/my/MySettings.vue'), meta: { title: '账号设置' } },
        ]
      },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('../views/OrderDetail.vue'), meta: { title: '订单详情', requireAuth: true } }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { title: '注册' } },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '控制台' } },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/ProductAudit.vue'), meta: { title: '商品审核' } },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'reports', name: 'AdminReports', component: () => import('../views/admin/ReportManage.vue'), meta: { title: '举报管理' } },
      { path: 'notices', name: 'AdminNotices', component: () => import('../views/admin/NoticeManage.vue'), meta: { title: '公告管理' } },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/CategoryManage.vue'), meta: { title: '分类管理' } },
    ]
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '闲置集市'} - 闲置集市`
  const userStore = useUserStore()
  if (to.meta.requireAuth && !userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.requireAdmin && !userStore.isAdmin) {
    next('/')
  } else {
    next()
  }
})

export default router
