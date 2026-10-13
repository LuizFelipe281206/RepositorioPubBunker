import axios from 'axios'

export default defineNuxtPlugin(() => {
    const config = useRuntimeConfig()

    const api = axios.create({
        baseURL: config.public.apiBase,
        timeout: 20000,
        withCredentials: true,
        headers: {
            'Content-Type': 'application/json'
        }
    })

    api.interceptors.request.use(async (request) => {
        const method =
            (request.method || 'get').toLowerCase()

        if (!['get', 'head', 'options'].includes(method)) {
            const { data } = await api.get('/auth/csrf')

            request.headers[data.headerName] = data.token
        }

        return request
    })

    return {
        provide: {
            api
        }
    }
})