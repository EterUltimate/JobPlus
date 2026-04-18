import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 路由懒加载
const LoginView = () => import('@/views/LoginView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')
const HomeView = () => import('@/views/HomeView.vue')
const JobListView = () => import('@/views/JobListView.vue')
const JobDetailView = () => import('@/views/JobDetailView.vue')
const MyDeliveriesView = () => import('@/views/MyDeliveriesView.vue')
const ResumeView = () => import('@/views/ResumeView.vue')
const HrDashboardView = () => import('@/views/HrDashboardView.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/home' },
    { path: '/login', component: LoginView, meta: { guest: true } },
    { path: '/register', component: RegisterView, meta: { guest: true } },
    { path: '/home', component: HomeView },
    { path: '/jobs', component: JobListView },
    { path: '/jobs/:id', component: JobDetailView },
    { path: '/my-deliveries', component: MyDeliveriesView, meta: { requiresAuth: true, role: 'SEEKER' } },
    { path: '/resume', component: ResumeView, meta: { requiresAuth: true, role: 'SEEKER' } },
    { path: '/hr/dashboard', component: HrDashboardView, meta: { requiresAuth: true, role: 'HR' } },
  ],
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.token) {
    next('/login')
  } else if (to.meta.role && auth.role !== to.meta.role && auth.role !== 'ADMIN') {
    next('/home')
  } else {
    next()
  }
})

export default router
