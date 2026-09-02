export default defineNuxtRouteMiddleware((to) => {
    if (import.meta.server) return

    const auth = useAuth()

    const {
        comandaAtiva,
        restaurarComanda
    } = useComanda()

    auth.restaurarSessao()
    restaurarComanda()

    const sessaoAtiva =
        auth.usuarioLogado.value ||
        comandaAtiva.value

    const roleAtual =
        comandaAtiva.value
            ? 'CLIENTE'
            : auth.role.value

    const paginaPublica =
    to.meta.public === true ||
    to.path.startsWith('/comanda/')

    const rotaInicial = () => {
        if (roleAtual === 'ADMIN') {
            return '/admin'
        }

        if (roleAtual === 'FUNCIONARIO') {
            return '/reservas'
        }

        return '/cardapio'
    }

    if (!sessaoAtiva && !paginaPublica) {
        return navigateTo('/login')
    }

    if (
        sessaoAtiva &&
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
        !rolesPermitidas.includes(roleAtual)
    ) {
        return navigateTo(rotaInicial())
    }
})