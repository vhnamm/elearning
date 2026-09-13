import path from 'node:path'
import { fileURLToPath } from 'node:url'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const srcDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), 'src')

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '~components': path.resolve(srcDir, 'components'),
      '~services': path.resolve(srcDir, 'services'),
      '~layouts': path.resolve(srcDir, 'components/layout'),
      '~context': path.resolve(srcDir, 'context'),
      '~hooks': path.resolve(srcDir, 'hooks'),
      '~pages': path.resolve(srcDir, 'pages'),
      '~assets': path.resolve(srcDir, 'assets'),
      '~': srcDir,
    },
  },
})
