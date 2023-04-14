import { createRouter, createWebHashHistory } from "vue-router";

const routes = [
    {
        path:'/',
        component: ()=>import('../pages/index.vue')
    },
    {
        path:'/index',
        redirect:'/'
    },
    {
        path:'/NormalIndex',
        component: ()=>import('../pages/NormalIndex.vue')
    },
]

const router = createRouter({
    history: createWebHashHistory(), // 路径格式
    routes: routes, // 路由数组
})

export default router