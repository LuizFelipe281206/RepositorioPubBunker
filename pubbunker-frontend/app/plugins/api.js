import axios from 'axios'

export default defineNuxtPlugin(() => {
    const config = useRuntimeConfig()

    const api = axios.create({
        baseURL: config.public.apiBase,
        timeout: 20000,
        headers: {
            'Content-Type': 'application/json'
        }
    })

    return {
        provide: {
            api
        }
    }
})