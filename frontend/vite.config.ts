import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '@app': path.resolve(__dirname, 'src/app'),
      '@shared': path.resolve(__dirname, 'src/shared'),
      '@features': path.resolve(__dirname, 'src/features'),
      '@components': path.resolve(__dirname, 'src/components'),
      '@lib': path.resolve(__dirname, 'src/lib'),
      '@stores': path.resolve(__dirname, 'src/stores'),
      '@veld': path.resolve(__dirname, 'src/generated'),
      // Force react/react-dom to resolve from the frontend's node_modules so
      // file:-linked workspace packages (e.g. @aether/payment-gateway) that
      // import "react/jsx-runtime" always find them, even in Docker where
      // the package lives outside the app's node_modules tree.
      react: path.resolve(__dirname, 'node_modules/react'),
      'react-dom': path.resolve(__dirname, 'node_modules/react-dom'),
      // Resolve payment gateway from source so Tailwind classes are always live
      // without needing a separate package build step.
      '@aether/payment-gateway': path.resolve(__dirname, '../packages/payment-gateway/src/index.ts'),
    },
    dedupe: ['react', 'react-dom'],
  },
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:9000',
        changeOrigin: true,
      },
    },
  },
});
