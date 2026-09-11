import { descreverErroComanda } from '~/utils/acessoComanda.js'

export const useComanda = () => {
    const erroComanda = useState('erro-comanda', () => null)
    const comandaAtual = useState('comanda-atual', () => null)
    const carregandoComanda = useState('carregando-comanda', () => false)

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
    }

    const salvarComanda = (comanda) => {
        if (codigoComanda.value !== comanda.codigoAcesso) {
            limparAtendimento()
        }

        comandaAtual.value = comanda

        if (import.meta.client) {
            sessionStorage.setItem('comanda', JSON.stringify(comanda))
        }
    }

    const limparComanda = () => {
        limparAtendimento()
        comandaAtual.value = null

        if (import.meta.client) {
            sessionStorage.removeItem('comanda')
        }
    }

    const restaurarComanda = () => {
        if (import.meta.server || comandaAtual.value) return

        const comandaSalva = sessionStorage.getItem('comanda')
        if (!comandaSalva) return

        try {
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
        carregandoComanda.value = true
        erroComanda.value = null

        try {
            const { data } = await $api.get(
                `/comandas/acesso/${encodeURIComponent(codigoAcesso)}`
            )

            salvarComanda(data)
            return true
        } catch (erro) {
            limparComanda()
            erroComanda.value = descreverErroComanda(erro)

            abrirPopup(
                erroComanda.value.titulo,
                erroComanda.value.mensagem,
                'erro'
            )

            return false
        } finally {
            carregandoComanda.value = false
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