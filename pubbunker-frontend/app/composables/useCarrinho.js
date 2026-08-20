export const useCarrinho = () => {
    const carrinho = useState(
        'carrinho',
        () => []
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const adicionar = (produto) => {
        carrinho.value.push(produto)

        abrirPopup(
            'Produto adicionado',
            `${produto.nome} foi adicionado ao pedido.`
        )
    }

    const remover = (index) => {
        carrinho.value.splice(index, 1)
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

        try {
            await $api.post('/pedidos', {
                clienteId: 3,
                produtosIds: carrinho.value.map(
                    produto => produto.id
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