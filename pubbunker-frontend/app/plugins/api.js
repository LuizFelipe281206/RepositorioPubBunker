import axios from 'axios'
import { resolverBaseApi } from '~/utils/enderecoApi.js'

export default defineNuxtPlugin(() => {
    const config = useRuntimeConfig()

    const api = axios.create({
        baseURL: resolverBaseApi(config.public.apiBase, import.meta.client ? window.location.origin : 'http://127.0.0.1:3000'),
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