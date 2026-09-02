export const usePedidosComanda = () => {
    const pedidosComanda = useState(
        'pedidos-comanda',
        () => []
    )

    const carregandoPedidosComanda = useState(
        'carregando-pedidos-comanda',
        () => false
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()
    const { codigoComanda } = useComanda()

    const carregarPedidosComanda = async (
        silencioso = false
    ) => {
        if (!codigoComanda.value) {
            pedidosComanda.value = []
            return
        }

        if (!silencioso) {
            carregandoPedidosComanda.value = true
        }

        try {
            const { data } = await $api.get(
                `/pedidos/comanda/${
                    encodeURIComponent(
                        codigoComanda.value
                    )
                }`
            )

            pedidosComanda.value = data
        } catch {
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