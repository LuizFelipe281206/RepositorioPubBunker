export const useCarrinho = () => {
    const carrinho = useState(
        'carrinho',
        () => []
    )

    const observacao = useState(
        'observacao-pedido',
        () => ''
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const { usuarioId } = useAuth()
    const { codigoComanda } = useComanda()

    const { pedidosComanda } =
        usePedidosComanda()

    const { pedidosCliente } =
        usePedidosCliente()

    const adicionar = (
        produto,
        quantidade = 1
    ) => {
        const quantidadeSelecionada = Math.max(
            1,
            Number(quantidade) || 1
        )

        const produtoExistente =
            carrinho.value.find(
                item => item.id === produto.id
            )

        if (produtoExistente) {
            produtoExistente.quantidade +=
                quantidadeSelecionada
        } else {
            carrinho.value.push({
                ...produto,
                quantidade: quantidadeSelecionada
            })
        }

        abrirPopup(
            'Produto adicionado',
            `${quantidadeSelecionada}x ${produto.nome} adicionado ao pedido.`
        )
    }

    const remover = (index) => {
        carrinho.value.splice(index, 1)
    }

    const cancelar = () => {
        carrinho.value = []
        observacao.value = ''
    }

    const finalizar = async () => {
        if (carrinho.value.length === 0) {
            abrirPopup(
                'Pedido vazio',
                'Adicione ao menos um produto.',
                'erro'
            )

            return
        }

        if (
            !codigoComanda.value &&
            !usuarioId.value
        ) {
            abrirPopup(
                'Sessão inválida',
                'Acesse novamente pelo QR Code da comanda.',
                'erro'
            )

            return
        }

        const identificacao = codigoComanda.value
            ? {
                codigoComanda:
                    codigoComanda.value
            }
            : {
                clienteId: usuarioId.value
            }

        try {
            const { data: pedidoCriado } =
                await $api.post(
                    '/pedidos',
                    {
                        ...identificacao,

                        observacao:
                            observacao.value.trim() ||
                            null,

                        itens: carrinho.value.map(
                            item => ({
                                produtoId: item.id,
                                quantidade:
                                    item.quantidade
                            })
                        )
                    }
                )

            if (codigoComanda.value) {
                const pedidoJaExiste =
                    pedidosComanda.value.some(
                        pedido =>
                            pedido.id === pedidoCriado.id
                    )

                if (!pedidoJaExiste) {
                    pedidosComanda.value.push(
                        pedidoCriado
                    )
                }
            } else {
                const pedidoJaExiste =
                    pedidosCliente.value.some(
                        pedido =>
                            pedido.id === pedidoCriado.id
                    )

                if (!pedidoJaExiste) {
                    pedidosCliente.value.unshift(
                        pedidoCriado
                    )
                }
            }

            carrinho.value = []
            observacao.value = ''

            abrirPopup(
                'Pedido realizado',
                'Seu pedido foi enviado com sucesso.'
            )

            return pedidoCriado
        } catch (erro) {
            abrirPopup(
                'Erro',
                erro.response?.data?.mensagem ||
                'Não foi possível finalizar o pedido.',
                'erro'
            )

            return null
        }
    }

    return {
        carrinho,
        observacao,
        adicionar,
        remover,
        cancelar,
        finalizar
    }
}