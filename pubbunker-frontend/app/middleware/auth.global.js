export default defineNuxtRouteMiddleware((to) => {
    if (import.meta.server) return

    const auth = useAuth()

    auth.restaurarSessao()

    const paginaPublica = to.meta.public === true

    const rotaInicial = () => {
        if (auth.role.value === 'ADMIN') {
            return '/admin'
        }

        if (auth.role.value === 'FUNCIONARIO') {
            return '/reservas'
        }

        return '/cardapio'
    }

    if (!auth.usuarioLogado.value && !paginaPublica) {
        return navigateTo('/login')
    }

    if (
        auth.usuarioLogado.value &&
        to.path === '/login'
    ) {
        return navigateTo(rotaInicial())
    }

    const rolesPermitidas =
        to.meta.roles ||
        (
            to.meta.role
                ? [to.meta.role]
                : []
        )

    if (
        rolesPermitidas.length &&
        !rolesPermitidas.includes(auth.role.value)
    ) {
        return navigateTo(rotaInicial())
    }
})