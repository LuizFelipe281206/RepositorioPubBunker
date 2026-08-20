import Aura from '@primeuix/themes/aura'
import { fileURLToPath } from 'node:url'

export default defineNuxtConfig({
    compatibilityDate: '2026-08-19',

    devtools: {
        enabled: true
    },

    ssr: false,

    modules: [
        '@primevue/nuxt-module'
    ],

    alias: {
        legacyAssets: fileURLToPath(
            new URL('./src/assets', import.meta.url)
        )
    },

    css: [
        'primeicons/primeicons.css',
        '~/assets/css/main.css'
    ],

    primevue: {
        options: {
            theme: {
                preset: Aura,
                options: {
                    darkModeSelector: false
                }
            }
        }
    },

    runtimeConfig: {
        public: {
            apiBase:
                process.env.NUXT_PUBLIC_API_BASE ||
                'http://localhost:8080'
        }
    }
})