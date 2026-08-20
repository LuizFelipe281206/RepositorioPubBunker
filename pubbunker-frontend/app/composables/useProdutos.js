export const useProdutos = () => {
    const produtos = useState(
        'produtos',
        () => []
    )

    const carregandoProdutos = useState(
        'carregando-produtos',
        () => false
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const carregarProdutos = async () => {
        carregandoProdutos.value = true

        try {
            const { data } = await $api.get('/produtos')

            produtos.value = data.filter(
                produto => produto.ativo
            )
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível carregar os produtos.',
                'erro'
            )
        } finally {
            carregandoProdutos.value = false
        }
    }

    const salvarProduto = async (
        produto,
        id = null
    ) => {
        if (id) {
            await $api.put(`/produtos/${id}`, produto)
        } else {
            await $api.post('/produtos', produto)
        }

        await carregarProdutos()
    }

    const excluirProduto = async (id) => {
        await $api.delete(`/produtos/${id}`)
        await carregarProdutos()
    }

    return {
        produtos,
        carregandoProdutos,
        carregarProdutos,
        salvarProduto,
        excluirProduto
    }
}