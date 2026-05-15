import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'https://yuliiasaltykova-mydogspace.hf.space',
        changeOrigin: true,
        secure: false,
      },
      '/chathub': {
        target: 'https://yuliiasaltykova-mydogspace.hf.space',
        ws: true,
        changeOrigin: true,
      },
      '/uploads': {
        target: 'https://yuliiasaltykova-mydogspace.hf.space',
        changeOrigin: true,
      },
    },
  },
})
