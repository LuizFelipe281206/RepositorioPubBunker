export const useCarrinho = () => {
    const carrinho = useState(
        'carrinho',
        () => []
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()
    const { usuarioId } = useAuth()

    const adicionar = (produto) => {
        const produtoExistente =
            carrinho.value.find(
                item => item.id === produto.id
            )

        if (produtoExistente) {
            produtoExistente.quantidade =
                (produtoExistente.quantidade || 1) + 1
        } else {
            carrinho.value.push({
                ...produto,
                quantidade: 1
            })
        }

        abrirPopup(
            'Produto adicionado',
            `${produto.nome} foi adicionado ao pedido.`
        )
    }

    const remover = (index) => {
        const item = carrinho.value[index]

        if (!item) return

        if (item.quantidade > 1) {
            item.quantidade--
        } else {
            carrinho.value.splice(index, 1)
        }
    }

    const cancelar = () => {
        carrinho.value = []
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

        if (!usuarioId.value) {
            abrirPopup(
                'Sessão inválida',
                'Entre novamente antes de finalizar o pedido.',
                'erro'
            )

            return
        }

        try {
            await $api.post('/pedidos', {
                clienteId: usuarioId.value,

                itens: carrinho.value.map(
                    item => ({
                        produtoId: item.id,
                        quantidade: item.quantidade
                    })
                )
            })

            carrinho.value = []

            abrirPopup(
                'Pedido realizado',
                'Seu pedido foi enviado com sucesso.'
            )
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível finalizar o pedido.',
                'erro'
            )
        }
    }

    return {
        carrinho,
        adicionar,
        remover,
        cancelar,
        finalizar
    }
}