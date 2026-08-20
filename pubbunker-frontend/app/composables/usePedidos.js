export const usePedidos = () => {
    const pedidos = useState(
        'pedidos',
        () => []
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const carregarPedidos = async () => {
        try {
            const { data } =
                await $api.get('/pedidos')

            pedidos.value = data
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível carregar os pedidos.',
                'erro'
            )
        }
    }

    const atualizarStatus = async (
        id,
        status
    ) => {
        try {
            await $api.patch(
                `/pedidos/status/${id}`,
                { status }
            )

            abrirPopup(
                'Status atualizado',
                'O status do pedido foi atualizado com sucesso.'
            )

            await carregarPedidos()
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível atualizar o status.',
                'erro'
            )
        }
    }

    const fecharPedido = async (pedido) => {
        if (pedido.status !== 'CONCLUIDO') {
            abrirPopup(
                'Ação não permitida',
                'O pedido só pode ser fechado quando estiver concluído.',
                'erro'
            )

            return
        }

        try {
            await $api.delete(
                `/pedidos/${pedido.id}`
            )

            abrirPopup(
                'Pedido fechado',
                'O pedido foi removido da tela administrativa.'
            )

            await carregarPedidos()
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível fechar o pedido.',
                'erro'
            )
        }
    }

    return {
        pedidos,
        carregarPedidos,
        atualizarStatus,
        fecharPedido
    }
}