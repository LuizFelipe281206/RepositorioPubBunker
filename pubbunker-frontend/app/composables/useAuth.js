export const useAuth = () => {
    const usuarioLogado = useState(
        'usuario-logado',
        () => false
    )

    const usuarioId = useState(
        'usuario-id',
        () => null
    )

    const role = useState(
        'usuario-role',
        () => ''
    )

    const nomeUsuario = useState(
        'usuario-nome',
        () => ''
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const rotaInicialPorRole = (roleUsuario) => {
        if (roleUsuario === 'ADMIN') {
            return '/admin'
        }

        if (roleUsuario === 'FUNCIONARIO') {
            return '/reservas'
        }

        return '/cardapio'
    }

    const restaurarSessao = () => {
        if (import.meta.server) return

        const idSalvo =
            localStorage.getItem('usuarioId')

        usuarioId.value =
            idSalvo ? Number(idSalvo) : null

        role.value =
            localStorage.getItem('role') || ''

        nomeUsuario.value =
            localStorage.getItem('nome') || ''

        usuarioLogado.value = Boolean(
            usuarioId.value &&
            role.value
        )
    }

    const login = async (credenciais) => {
        try {
            const { data } = await $api.post(
                '/auth/login',
                credenciais
            )

            localStorage.setItem(
                'usuarioId',
                String(data.id)
            )

            localStorage.setItem(
                'role',
                data.role
            )

            localStorage.setItem(
                'nome',
                data.nome
            )

            usuarioId.value = data.id
            role.value = data.role
            nomeUsuario.value = data.nome
            usuarioLogado.value = true

            abrirPopup(
                'Sucesso',
                'Login realizado com sucesso!'
            )

            await navigateTo(
                rotaInicialPorRole(data.role)
            )
        } catch {
            abrirPopup(
                'Erro',
                'Email ou senha inválidos.',
                'erro'
            )
        }
    }

    const logout = async () => {
        localStorage.removeItem('usuarioId')
        localStorage.removeItem('role')
        localStorage.removeItem('nome')

        usuarioId.value = null
        usuarioLogado.value = false
        role.value = ''
        nomeUsuario.value = ''

        await navigateTo('/login')
    }

    return {
        usuarioLogado,
        usuarioId,
        role,
        nomeUsuario,
        restaurarSessao,
        login,
        logout
    }
}