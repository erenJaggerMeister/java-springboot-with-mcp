import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'mfe_chat',
      filename: 'remoteEntry.js',
      exposes: {
        './ChatApp': './src/ChatApp.jsx',
      },
      shared: ['react', 'react-dom']
    })
  ],
  server: {
    port: 3003,
    strictPort: true,
    cors: true
  },
  preview: {
    port: 3003,
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
