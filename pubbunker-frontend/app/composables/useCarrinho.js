export const useCarrinho = () => {
    const carrinho = useState('carrinho', () => [])
    const observacao = useState('observacao-pedido', () => '')

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const { codigoComanda, tratarAcessoInvalido } = useComanda()
    const { pedidosComanda } = usePedidosComanda()

    const adicionar = (produto, quantidade = 1, adicionais = []) => {
        const quantidadeSelecionada = Math.max(
            1,
            Number(quantidade) || 1
        )

        const adicionaisUnicos = (
            Array.isArray(adicionais) ? adicionais : []
        ).filter(
            (adicional, indice, lista) =>
                lista.findIndex(item => item.id === adicional.id) === indice
        )

        const adicionaisIds = adicionaisUnicos
            .map(adicional => adicional.id)
            .sort((a, b) => a - b)

        const chaveConfiguracao =
            `${produto.id}:${adicionaisIds.join('-')}`

        const produtoExistente = carrinho.value.find(
            item => item.chaveConfiguracao === chaveConfiguracao
        )

        if (produtoExistente) {
            produtoExistente.quantidade += quantidadeSelecionada
        } else {
            const valorAdicionais = adicionaisUnicos.reduce(
                (soma, adicional) => soma + Number(adicional.preco),
                0
            )

            carrinho.value.push({
                ...produto,
                quantidade: quantidadeSelecionada,
                adicionaisSelecionados: adicionaisUnicos,
                precoUnitario: Number(produto.preco) + valorAdicionais,
                chaveConfiguracao
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

        if (!codigoComanda.value) {
            abrirPopup(
                'Comanda obrigatória',
                'Acesse o cardápio pelo QR Code de uma comanda em uso.',
                'erro'
            )
            return
        }

        const codigoSolicitado = codigoComanda.value

        try {
            const { data: pedidoCriado } = await $api.post('/pedidos', {
                codigoComanda: codigoSolicitado,
                observacao: observacao.value.trim() || null,
                itens: carrinho.value.map(item => ({
                    produtoId: item.id,
                    quantidade: item.quantidade,
                    adicionaisIds: (
                        item.adicionaisSelecionados || []
                    ).map(adicional => adicional.id)
                }))
            })

            if (codigoSolicitado !== codigoComanda.value) return null

            const pedidoJaExiste = pedidosComanda.value.some(
                pedido => pedido.id === pedidoCriado.id
            )

            if (!pedidoJaExiste) {
                pedidosComanda.value.push(pedidoCriado)
            }

            carrinho.value = []
            observacao.value = ''

            abrirPopup(
                'Pedido realizado',
                'Seu pedido foi enviado com sucesso.'
            )

            return pedidoCriado
        } catch (erro) {
            if (codigoSolicitado !== codigoComanda.value) return null

            if (tratarAcessoInvalido(erro, codigoSolicitado)) return null

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