import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
    plugins: [react()],
    root: './src/main/resources/static',
    server: {
        proxy: {
            '/api': 'http://localhost:8080'
        }
    },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    },
    build: {
        outDir: '../static/assets',
        emptyOutDir: true,
        assetsDir: '',
        rollupOptions: {
            input: {
                main: path.resolve(__dirname, 'src/main/resources/static/main.jsx'),
            },
            output: {
                entryFileNames: '[name].js',
                chunkFileNames: '[name].js',
                assetFileNames: '[name][extname]',
            },
        },
    }
})