import { descreverErroComanda } from '~/utils/acessoComanda.js'

export const useComanda = () => {
    const erroComanda = useState('erro-comanda', () => null)
    const comandaAtual = useState('comanda-atual', () => null)
    const carregandoComanda = useState('carregando-comanda', () => false)
    const versaoAcesso = useState('versao-acesso-comanda', () => 0)
    const exigeCodigoComanda = useState('exige-codigo-comanda', () => false)

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
        useState('erro-pedidos-comanda', () => null).value = null
        useState('carregando-pedidos-comanda', () => false).value = false
        useState('versao-consulta-pedidos-comanda', () => 0).value++
        useState('avaliacoes', () => []).value = []
        useState('erro-avaliacoes', () => null).value = null
        useState('carregando-avaliacoes', () => false).value = false
        useState('versao-consulta-avaliacoes', () => 0).value++
    }

    const salvarComanda = (comanda) => {
        if (codigoComanda.value !== comanda.codigoAcesso) {
            limparAtendimento()
        }

        comandaAtual.value = comanda
        exigeCodigoComanda.value = false

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
        exigeCodigoComanda.value = false
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

    const acessarComanda = async (codigoAcesso, codigoEntrada = null) => {
        const versao = ++versaoAcesso.value
        const formularioVisivel = exigeCodigoComanda.value
        if (codigoEntrada === null) exigeCodigoComanda.value = false
        carregandoComanda.value = true
        erroComanda.value = null

        try {
            const { data } = await $api.post(
                `/comandas/acesso/${encodeURIComponent(codigoAcesso)}`,
                { codigoEntrada }
            )

            if (versao !== versaoAcesso.value) return false

            salvarComanda(data)
            return true
        } catch (erro) {
            if (versao !== versaoAcesso.value) return false

            limparComanda()
            erroComanda.value = descreverErroComanda(erro)

            const codigoErro = erro.response?.data?.codigo
            if (['CODIGO_COMANDA_NECESSARIO', 'CODIGO_COMANDA_INVALIDO'].includes(codigoErro)) {
                exigeCodigoComanda.value = true
                return false
            }

            exigeCodigoComanda.value = formularioVisivel && erro.response?.status === 429

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
        const codigoErro = erro.response?.data?.codigo
        if (
            codigoSolicitado !== codigoComanda.value ||
            !['ACESSO_COMANDA_INVALIDO', 'CODIGO_COMANDA_NECESSARIO'].includes(codigoErro)
        ) {
            return false
        }

        limparComanda()

        if (codigoErro === 'CODIGO_COMANDA_NECESSARIO') {
            navigateTo(`/comanda/${encodeURIComponent(codigoSolicitado)}`)
            return true
        }

        abrirPopup(
            'Atendimento encerrado',
            erro.response.data.mensagem,
            'erro'
        )

        return true
    }

    const sairComanda = () => useAuth().logout()

    const cancelarAcessoComanda = () => {
        versaoAcesso.value++
        carregandoComanda.value = false
    }

    return {
        comandaAtual,
        codigoComanda,
        comandaAtiva,
        carregandoComanda,
        erroComanda,
        exigeCodigoComanda,
        cancelarAcessoComanda,
        restaurarComanda,
        acessarComanda,
        limparComanda,
        tratarAcessoInvalido,
        sairComanda
    }
}
