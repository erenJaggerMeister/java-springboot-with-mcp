import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'mfe_user',
      filename: 'remoteEntry.js',
      exposes: {
        './UserApp': './src/UserApp.jsx',
      },
      shared: ['react', 'react-dom']
    })
  ],
  server: {
    port: 3002,
    strictPort: true,
    cors: true
  },
  preview: {
    port: 3002,
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
