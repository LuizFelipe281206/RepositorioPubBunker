import Aura from '@primeuix/themes/aura'
import { fileURLToPath } from 'node:url'

export default defineNuxtConfig({
    compatibilityDate: '2026-08-19',

    devtools: { enabled: false },

    ssr: false,

    app: {
        head: {
            title: 'PubBunker',
            htmlAttrs: { lang: 'pt-BR' },
            meta: [{ name: 'description', content: 'Cardápio, pedidos e atendimento do PubBunker.' }],
            link: [
                { rel: 'icon', type: 'image/png', href: '/pubbunker-icon.png' },
                { rel: 'apple-touch-icon', href: '/pubbunker-icon.png' }
            ]
        }
    },

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
        apiBase: '',
        appBaseUrl: ''
    }
}
})