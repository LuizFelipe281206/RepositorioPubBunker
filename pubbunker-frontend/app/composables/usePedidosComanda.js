import { mensagemErro } from '~/utils/catalogo.js'

export const usePedidosComanda = () => {
    const pedidosComanda = useState('pedidos-comanda', () => [])
    const erroPedidosComanda = useState('erro-pedidos-comanda', () => null)
    const versaoConsulta = useState('versao-consulta-pedidos-comanda', () => 0)

    const carregandoPedidosComanda = useState(
        'carregando-pedidos-comanda',
        () => false
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()
    const { codigoComanda, tratarAcessoInvalido } = useComanda()

    const registrarPedido = pedido => {
        versaoConsulta.value++
        carregandoPedidosComanda.value = false

        if (!pedidosComanda.value.some(item => item.id === pedido.id)) {
            pedidosComanda.value = [...pedidosComanda.value, pedido]
        }
    }

    const carregarPedidosComanda = async (silencioso = false) => {
        const versao = ++versaoConsulta.value

        if (!codigoComanda.value) {
            pedidosComanda.value = []
            erroPedidosComanda.value = null
            carregandoPedidosComanda.value = false
            return
        }

        if (!silencioso) {
            carregandoPedidosComanda.value = true
        }

        const codigoSolicitado = codigoComanda.value

        try {
            const { data } = await $api.get(
                `/pedidos/comanda/${encodeURIComponent(codigoSolicitado)}`
            )

            if (
                versao === versaoConsulta.value &&
                codigoSolicitado === codigoComanda.value
            ) {
                pedidosComanda.value = data
                erroPedidosComanda.value = null
            }
        } catch (erro) {
            if (
                versao !== versaoConsulta.value ||
                codigoSolicitado !== codigoComanda.value
            ) return

            if (tratarAcessoInvalido(erro, codigoSolicitado)) return

            erroPedidosComanda.value = mensagemErro(
                erro,
                'Não foi possível carregar os pedidos da comanda. Tente atualizar novamente.'
            )

            if (!silencioso) {
                abrirPopup(
                    'Não foi possível carregar seus pedidos',
                    erroPedidosComanda.value,
                    'erro',
                    erro.response?.status === 429
                        ? erro.response.data?.aguardeSegundos || 60 : null
                )
            }
        } finally {
            if (versao === versaoConsulta.value) {
                carregandoPedidosComanda.value = false
            }
        }
    }

    return {
        pedidosComanda,
        erroPedidosComanda,
        carregandoPedidosComanda,
        carregarPedidosComanda,
        registrarPedido
    }
}
