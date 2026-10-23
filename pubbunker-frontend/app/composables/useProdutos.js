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

            produtos.value = Array.isArray(data) ? data : []
        } catch {
            abrirPopup(
                'Não foi possível carregar os produtos',
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
        const { data } = id
            ? await $api.put(`/produtos/${id}`, produto)
            : await $api.post('/produtos', produto)
        const indice = produtos.value.findIndex(item => item.id === data.id)
        if (indice >= 0) produtos.value[indice] = data
        else produtos.value.push(data)
        return data
    }

    const excluirProduto = async id => {
        await $api.delete(`/produtos/${id}`)
        produtos.value = produtos.value.filter(item => item.id !== id)
    }

    return {
        produtos,
        carregandoProdutos,
        carregarProdutos,
        salvarProduto,
        excluirProduto
    }
}