export const useAuth = () => {
    const usuarioLogado = useState(
        'usuario-logado',
        () => false
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

    const restaurarSessao = () => {
        if (import.meta.server) return

        role.value =
            localStorage.getItem('role') || ''

        nomeUsuario.value =
            localStorage.getItem('nome') || ''

        usuarioLogado.value = Boolean(role.value)
    }

    const login = async (credenciais) => {
        try {
            const { data } = await $api.post(
                '/auth/login',
                credenciais
            )

            localStorage.setItem('role', data.role)
            localStorage.setItem('nome', data.nome)

            role.value = data.role
            nomeUsuario.value = data.nome
            usuarioLogado.value = true

            abrirPopup(
                'Sucesso',
                'Login realizado com sucesso!'
            )

            await navigateTo(
                data.role === 'ADMIN'
                    ? '/admin'
                    : '/cardapio'
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
        localStorage.removeItem('role')
        localStorage.removeItem('nome')

        usuarioLogado.value = false
        role.value = ''
        nomeUsuario.value = ''

        await navigateTo('/login')
    }

    return {
        usuarioLogado,
        role,
        nomeUsuario,
        restaurarSessao,
        login,
        logout
    }
}