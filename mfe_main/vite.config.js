import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'mfe_main',
      remotes: {
        mfe_student: 'http://localhost:3001/assets/remoteEntry.js',
        mfe_user: 'http://localhost:3002/assets/remoteEntry.js',
        mfe_chat: 'http://localhost:3003/assets/remoteEntry.js',
        mfe_ai_config: 'http://localhost:3004/assets/remoteEntry.js',
      },
      shared: ['react', 'react-dom']
    })
  ],
  server: {
    port: 3000,
    strictPort: true,
    cors: true
  },
  preview: {
    port: 3000,
    strictPort: true,
    cors: true
  },
  build: {
    target: 'esnext',
    modulePreload: false,
    minify: false,
    cssCodeSplit: false
  }
});
