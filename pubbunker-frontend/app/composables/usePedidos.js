export const usePedidos = () => {
    const pedidos = useState(
        'pedidos',
        () => []
    )

    const carregandoPedidos = useState(
        'carregando-pedidos',
        () => false
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const carregarPedidos = async (
        silencioso = false
    ) => {
        if (!silencioso) {
            carregandoPedidos.value = true
        }

        try {
            const { data } =
                await $api.get('/pedidos')

            pedidos.value =
                Array.isArray(data) ? data : []
        } catch {
            if (!silencioso) {
                abrirPopup(
                    'Erro',
                    'Não foi possível carregar os pedidos.',
                    'erro'
                )
            }
        } finally {
            if (!silencioso) {
                carregandoPedidos.value = false
            }
        }
    }

    const atualizarStatus = async (
        id,
        status
    ) => {
        try {
            const { data: pedidoAtualizado } =
                await $api.patch(
                    `/pedidos/status/${id}`,
                    { status }
                )

            const indice = pedidos.value.findIndex(
                pedido => pedido.id === id
            )

            if (indice >= 0) {
                pedidos.value[indice] =
                    pedidoAtualizado
            }

            abrirPopup(
                'Status atualizado',
                'O status do pedido foi atualizado com sucesso.'
            )

            return pedidoAtualizado
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível atualizar o status.',
                'erro'
            )

            return null
        }
    }

    const fecharPedido = async (pedido) => {
        if (pedido.status !== 'CONCLUIDO') {
            abrirPopup(
                'Ação não permitida',
                'O pedido só pode ser fechado quando estiver concluído.',
                'erro'
            )

            return false
        }

        try {
            await $api.delete(
                `/pedidos/${pedido.id}`
            )

            pedidos.value = pedidos.value.filter(
                pedidoAtual =>
                    pedidoAtual.id !== pedido.id
            )

            abrirPopup(
                'Pedido fechado',
                'O pedido foi removido da tela administrativa.'
            )

            return true
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível fechar o pedido.',
                'erro'
            )

            return false
        }
    }

    return {
        pedidos,
        carregandoPedidos,
        carregarPedidos,
        atualizarStatus,
        fecharPedido
    }
}