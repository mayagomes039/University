import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import NotFoundComponent from '@/views/NotFound.vue'
import Register from '@/views/Register.vue'
import HomeLogged from '@/views/HomeLogged.vue'
import UserEditProfile from '@/views/EditProfile.vue'
import BecomePremium from '@/views/BecomePremium.vue'
import UserProfile from "@/views/UserProfile.vue";
import piDetails from "@/views/PiDetails.vue";
import photoGallery from "@/views/PhotoGallery.vue"
import Review from '../views/Review.vue'
import Create from '../views/Create.vue'
import {UserStore} from "@/store/userStore.js";
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
  },
  {
    path: '/home',
    name: 'HomeLogged',
    component: HomeLogged,
    meta: { requiresAuth: true },
  },
  {
    path: '/editProfile',
    name: 'UserProfile',
    component: UserEditProfile,
    meta: { requiresAuth: true },
  },
  {
    path: '/subscribe/premium',
    name: 'BecomePremium',
    component: BecomePremium,
    meta: { requiresAuth: true },
  },
  {
    path: '/profile/:id',
    name: 'User Profile',
    component: UserProfile,
    meta: { requiresAuth: true },
  },
  {
    path: '/history',
    name: 'History',
    component: UserProfile,
    meta: { requiresAuth: true },
  },
  {
    path: '/pi/details/:id',
    name: 'piDetails',
    component: piDetails,
    meta: { requiresAuth: true },
  },
  {
    path: '/photoGallery/:id',
    name: 'photoGallery',
    component: photoGallery,
    meta: { requiresAuth: true },
  },
  {
    path: '/review',
    name: 'Review',
    component: Review,
    meta:{ requiresAuth:true }
  },
  {
    path: '/create',
    name: 'Create',
    component: Create,
    meta:{requiresAuth:true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'CatchAll',
    component: NotFoundComponent,
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})


// 🔐 Global auth guard
router.beforeEach((to, from, next) => {
  // const token = localStorage.getItem('authToken')

  const userStore = UserStore()

  const token = userStore.authToken


  const isLoggedIn = !!token

  // Redirect to /home if already logged in and trying to access public routes
  if (isLoggedIn && (to.name === 'Home' || to.name === 'Login' || to.name === 'Register')) {
    next('/home')
  }

  // Redirect to login if trying to access protected routes without token
  else if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  }

  // All other cases allowed
  else {
    next()
  }
})

export default router
