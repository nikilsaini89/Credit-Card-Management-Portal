// vite.config.ts
import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false
      }
    }
  },
  test: {
    environment: "jsdom",
    globals: false,
    setupFiles: "./tests/setup.ts",
    include: ["tests/unit/**/*.spec.ts","src/**/*.spec.ts"],
    watch: false
  }
})
