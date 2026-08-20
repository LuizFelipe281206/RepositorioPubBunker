export default defineNuxtRouteMiddleware((to) => {
    if (import.meta.server) return

    const auth = useAuth()

    auth.restaurarSessao()

    const paginaPublica = to.meta.public === true

    if (!auth.usuarioLogado.value && !paginaPublica) {
        return navigateTo('/login')
    }

    if (
        auth.usuarioLogado.value &&
        to.path === '/login'
    ) {
        return navigateTo(
            auth.role.value === 'ADMIN'
                ? '/admin'
                : '/cardapio'
        )
    }

    if (
        to.meta.role &&
        auth.role.value !== to.meta.role
    ) {
        return navigateTo('/cardapio')
    }
})