export const useAuth = () => {
    const usuarioLogado =
        useState('usuario-logado', () => false)

    const usuarioId =
        useState('usuario-id', () => null)

    const role =
        useState('usuario-role', () => '')

    const nomeUsuario =
        useState('usuario-nome', () => '')

    const versaoSessao =
        useState('versao-sessao', () => 0)

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const limparDadosAntigos = () => {
        if (import.meta.client) {
            try {
                for (const chave of ['usuarioId', 'role', 'nome']) {
                    localStorage.removeItem(chave)
                }
            } catch {
                // O login não depende desses dados antigos.
            }
        }
    }

    const limparSessao = () => {
        versaoSessao.value++

        usuarioLogado.value = false
        usuarioId.value = null
        role.value = ''
        nomeUsuario.value = ''

        limparDadosAntigos()
    }

    const aplicarSessao = (usuario) => {
        versaoSessao.value++

        usuarioId.value = usuario.id
        role.value = usuario.role
        nomeUsuario.value = usuario.nome
        usuarioLogado.value = true

        limparDadosAntigos()
    }

    const rotaInicialPorRole = (perfil) => {
        if (perfil === 'ADMIN') return '/admin'
        if (perfil === 'FUNCIONARIO') return '/reservas'

        return '/cardapio'
    }

    const restaurarSessao = async () => {
        if (import.meta.server) return false

        const versao = versaoSessao.value

        try {
            const { data } = await $api.get('/auth/me')

            if (versao !== versaoSessao.value) {
                return usuarioLogado.value
            }

            aplicarSessao(data)
            return true

        } catch {
            if (versao === versaoSessao.value) {
                limparSessao()
            }

            return usuarioLogado.value
        }
    }

    const login = async (credenciais) => {
        try {
            const { data } = await $api.post(
                '/auth/login',
                credenciais
            )

            aplicarSessao(data)

            abrirPopup(
                'Sucesso',
                'Login realizado com sucesso!'
            )

            await navigateTo(
                rotaInicialPorRole(data.role)
            )

            return true

        } catch (erro) {
            const status = erro.response?.status

            let mensagem =
                'Não foi possível conectar ao servidor. Verifique a conexão.'

            if (status === 401) {
                mensagem = 'Email ou senha inválidos.'
            } else if (status === 400) {
                mensagem = 'Confira o email e a senha informados.'
            } else if (status === 403) {
                mensagem =
                    'Não foi possível validar a requisição. Atualize a página.'
            } else if (status) {
                mensagem =
                    'O servidor não conseguiu concluir o login. Tente novamente.'
            }

            abrirPopup('Erro', mensagem, 'erro')
            return false
        }
    }

    const logout = async () => {
        try {
            await $api.post('/auth/logout')

            limparSessao()
            useComanda().limparComanda()

            await navigateTo('/login')
            return true

        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível encerrar a sessão. Tente novamente.',
                'erro'
            )

            return false
        }
    }

    return {
        usuarioLogado,
        usuarioId,
        role,
        nomeUsuario,
        restaurarSessao,
        login,
        logout,
        rotaInicialPorRole
    }
}