export const usePedidosCliente = () => {
    const pedidosCliente = useState(
        'pedidos-cliente',
        () => []
    )

    const carregandoPedidosCliente = useState(
        'carregando-pedidos-cliente',
        () => false
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()
    const { usuarioId } = useAuth()

    const carregarPedidosCliente = async (
        silencioso = false
    ) => {
        if (!usuarioId.value) {
            pedidosCliente.value = []
            return
        }

        if (!silencioso) {
            carregandoPedidosCliente.value = true
        }

        try {
            const { data } = await $api.get(
                `/pedidos/cliente/${usuarioId.value}`
            )

            pedidosCliente.value =
                Array.isArray(data) ? data : []
        } catch {
            pedidosCliente.value = []

            if (!silencioso) {
                abrirPopup(
                    'Erro',
                    'Não foi possível carregar os pedidos do cliente.',
                    'erro'
                )
            }
        } finally {
            if (!silencioso) {
                carregandoPedidosCliente.value = false
            }
        }
    }

    return {
        pedidosCliente,
        carregandoPedidosCliente,
        carregarPedidosCliente
    }
}