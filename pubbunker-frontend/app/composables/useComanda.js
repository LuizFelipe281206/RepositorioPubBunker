import { descreverErroComanda } from '~/utils/acessoComanda.js'

export const useComanda = () => {
    const erroComanda = useState('erro-comanda', () => null)
    const comandaAtual = useState('comanda-atual', () => null)
    const carregandoComanda = useState('carregando-comanda', () => false)
    const versaoAcesso = useState('versao-acesso-comanda', () => 0)

    const codigoComanda = computed(
        () => comandaAtual.value?.codigoAcesso || ''
    )

    const comandaAtiva = computed(
        () => comandaAtual.value?.status === 'EM_USO'
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const limparAtendimento = () => {
    useState('carrinho', () => []).value = []
    useState('observacao-pedido', () => '').value = ''
    useState('pedidos-comanda', () => []).value = []
    useState('avaliacoes', () => []).value = []
    useState('carregando-avaliacoes', () => false).value = false
    useState('versao-consulta-avaliacoes', () => 0).value++
}

    const salvarComanda = (comanda) => {
        if (codigoComanda.value !== comanda.codigoAcesso) {
            limparAtendimento()
        }

        comandaAtual.value = comanda

        if (import.meta.client) {
            try {
                sessionStorage.setItem('comanda', JSON.stringify(comanda))
            } catch {
                // O atendimento continua na memória se o navegador bloquear o armazenamento.
            }
        }
    }

    const limparComanda = () => {
        versaoAcesso.value++
        carregandoComanda.value = false
        limparAtendimento()
        comandaAtual.value = null

        if (import.meta.client) {
            try {
                sessionStorage.removeItem('comanda')
            } catch {
                // Os dados em memória já foram limpos.
            }
        }
    }

    const restaurarComanda = () => {
        if (import.meta.server || comandaAtual.value) return

        try {
            const comandaSalva = sessionStorage.getItem('comanda')
            if (!comandaSalva) return

            const comanda = JSON.parse(comandaSalva)

            if (comanda.status === 'EM_USO') {
                comandaAtual.value = comanda
            } else {
                limparComanda()
            }
        } catch {
            limparComanda()
        }
    }

    const acessarComanda = async (codigoAcesso) => {
        const versao = ++versaoAcesso.value
        carregandoComanda.value = true
        erroComanda.value = null

        try {
            const { data } = await $api.get(
                `/comandas/acesso/${encodeURIComponent(codigoAcesso)}`
            )

            if (versao !== versaoAcesso.value) return false

            salvarComanda(data)
            return true
        } catch (erro) {
            if (versao !== versaoAcesso.value) return false

            limparComanda()
            erroComanda.value = descreverErroComanda(erro)

            abrirPopup(
    erroComanda.value.titulo,
    erroComanda.value.mensagem,
    'erro',
    erro.response?.status === 429 ? 60 : null
)

            return false
        } finally {
            if (versao === versaoAcesso.value) {
                carregandoComanda.value = false
            }
        }
    }

    const tratarAcessoInvalido = (erro, codigoSolicitado) => {
        if (
            codigoSolicitado !== codigoComanda.value ||
            erro.response?.data?.codigo !== 'ACESSO_COMANDA_INVALIDO'
        ) {
            return false
        }

        limparComanda()

        abrirPopup(
            'Atendimento encerrado',
            erro.response.data.mensagem,
            'erro'
        )

        return true
    }

    const sairComanda = async () => {
        limparComanda()
        await navigateTo('/login')
    }

    return {
        comandaAtual,
        codigoComanda,
        comandaAtiva,
        carregandoComanda,
        erroComanda,
        restaurarComanda,
        acessarComanda,
        limparComanda,
        tratarAcessoInvalido,
        sairComanda
    }
}
