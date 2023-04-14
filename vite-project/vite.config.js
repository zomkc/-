import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server:{
    host:true,
    port:5555,
    proxy:{
      '/bbs':{
        target:'http://localhost:8989',
        // target:'https://zomkc.cn:8085',
        changeOrigin:true,  // 允许跨域
        rewrite: (path) => path.replace(/^\/bbs/,'')
      }
    }
  }
})

