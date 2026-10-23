import { prepararInclusao, validarCarrinho, mensagemErro } from '~/utils/catalogo.js'

export const useCarrinho = () => {
    const carrinho = useState('carrinho', () => [])
    const enviandoPedido = useState('enviando-pedido', () => false)
    const observacao = useState('observacao-pedido', () => '')

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const { codigoComanda, tratarAcessoInvalido } = useComanda()
    const { pedidosComanda } = usePedidosComanda()

    const adicionar = (produto, quantidade = 1, adicionais = []) => {
        if (enviandoPedido.value) return
        const resultado = prepararInclusao(carrinho.value, produto, quantidade, adicionais)
        if (resultado.erro) {
            abrirPopup('Confira seu pedido', resultado.erro, 'erro')
            return false
        }
        carrinho.value = resultado.itens

        abrirPopup(
            'Produto adicionado',
            `${quantidade}x ${produto.nome} adicionado ao pedido.`
        )
    }

    const remover = (index) => {
        if (enviandoPedido.value) return
        carrinho.value.splice(index, 1)
    }

    const cancelar = () => {
        if (enviandoPedido.value) return
        carrinho.value = []
        observacao.value = ''
    }

    const finalizar = async () => {
        if (enviandoPedido.value) return null
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

        const problema = validarCarrinho(carrinho.value, observacao.value)
        if (problema) {
            abrirPopup('Confira seu pedido', problema, 'erro')
            return null
        }

        enviandoPedido.value = true
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
                erro.response?.data?.codigo === 'LIMITE_PEDIDO_EXCEDIDO'
                    ? 'Pedido acima do limite' : 'Não foi possível enviar o pedido',
                mensagemErro(erro, 'Não foi possível finalizar o pedido. Tente novamente.'),
                'erro',
            erro.response?.status === 429 ? 60 : null
            )

            return null
        } finally {
            enviandoPedido.value = false
        }
    }

    return {
        carrinho,
        enviandoPedido,
        observacao,
        adicionar,
        remover,
        cancelar,
        finalizar
    }
}