/**
 * router/index.ts
 *
 * Automatic routes for `./src/pages/*.vue`
 */

// Composables
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router/auto'
import { setupLayouts } from 'virtual:generated-layouts'
import { getUser } from '@/utils/functions'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/projects',
  },
  {
    path: '/login',
    component: () => import('@/pages/Login.vue'),
  },
  {
    path: '/subscriptions',
    component: () => import('@/pages/Subscriptions.vue'), 
  },
  {
    path: '/projects',
    name: 'ProjectsList',
    component: () => import('@/pages/ProjectsList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id',
    name: 'ProjectDetails',
    component: () => import('@/pages/Project.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/register',
    component: () => import('@/pages/Register.vue')
  },
  {
    path: '/registerAnonymous',
    component: () => import('@/pages/RegisterAnonymous.vue')
  },
  {
    path: '/choosesubscriptions',
    component: () => import('@/pages/ChooseSubscription.vue'),
    meta: { requiresAuth: true }, 
  },
  {
    path: '/chooseRegister',
    component: () => import('@/pages/ChooseRegister.vue'),
  },
  {
    path: '/editprofile',
    component: () => import('@/pages/EditProfile.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/payment',
    component: () => import('@/pages/Payment.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/perfil',
    component: () => import('@/pages/Perfil.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/viewSubscriptions',
    component: () => import('@/pages/ViewSubscriptions.vue'),
    //meta: { requiresAuth: true },
  },
  
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: setupLayouts(routes),
})

// to check for login on pages
/*
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!getUser();

  if(to.meta.requiresAuth && !isAuthenticated && window.localStorage.getItem('type') !== 'anonymous') {
    next({ path: 'login'});
  } else if (to.meta.requiresAuth && window.localStorage.getItem('type') === 'anonymous' && to.path !== '/projects') {
    next({ path: 'login'});
  }
  else {
    next();
  }
})*/
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!getUser();
  const isAnonymous = window.localStorage.getItem('type') === 'anonymous';
  // had to name those two so i can match them here
  const allowedAnonymousRoutes = ['ProjectsList', 'ProjectDetails', 'ViewSubscriptions']; // Use route names here

  if (to.meta.requiresAuth) {
    if (isAnonymous) {
      if (allowedAnonymousRoutes.includes(to.name)) {
        next(); 
      } else {
        next('/login'); 
      }
    } else if (!isAuthenticated) {
      next('/login'); 
    } else {
      next(); 
    }
  } else {
    if (to.path === '/viewSubscriptions' && (isAnonymous || !isAuthenticated)) {
      next();
    } else {
      next();
    }
  }
});

// Workaround for https://github.com/vitejs/vite/issues/11804
router.onError((err, to) => {
  if (err?.message?.includes?.('Failed to fetch dynamically imported module')) {
    if (!localStorage.getItem('vuetify:dynamic-reload')) {
      console.log('Reloading page to fix dynamic import error')
      localStorage.setItem('vuetify:dynamic-reload', 'true')
      location.assign(to.fullPath)
    } else {
      console.error('Dynamic import error, reloading page did not fix it', err)
    }
  } else {
    console.error(err)
  }
})

router.isReady().then(() => {
  localStorage.removeItem('vuetify:dynamic-reload')
})

export default router
