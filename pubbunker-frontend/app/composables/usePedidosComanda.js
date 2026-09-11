export const usePedidosComanda = () => {
    const pedidosComanda = useState('pedidos-comanda', () => [])

    const carregandoPedidosComanda = useState(
        'carregando-pedidos-comanda',
        () => false
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()
    const { codigoComanda, tratarAcessoInvalido } = useComanda()

    const carregarPedidosComanda = async (silencioso = false) => {
        if (!codigoComanda.value) {
            pedidosComanda.value = []
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

            if (codigoSolicitado === codigoComanda.value) {
                pedidosComanda.value = data
            }
        } catch (erro) {
            if (codigoSolicitado !== codigoComanda.value) return

            if (tratarAcessoInvalido(erro, codigoSolicitado)) return

            if (!silencioso) {
                abrirPopup(
                    'Erro',
                    'Não foi possível carregar os pedidos da comanda.',
                    'erro'
                )
            }
        } finally {
            if (!silencioso) {
                carregandoPedidosComanda.value = false
            }
        }
    }

    return {
        pedidosComanda,
        carregandoPedidosComanda,
        carregarPedidosComanda
    }
}