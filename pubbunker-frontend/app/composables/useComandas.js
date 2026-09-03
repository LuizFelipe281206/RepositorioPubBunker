export const useComandas = () => {
    const comandas = useState(
        'comandas',
        () => []
    )

    const carregandoComandas = useState(
        'carregando-comandas',
        () => false
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const ordenarComandas = () => {
        comandas.value.sort(
            (a, b) => Number(a.numero) - Number(b.numero)
        )
    }

    const atualizarNaLista = (comandaAtualizada) => {
        const indice = comandas.value.findIndex(
            comanda => comanda.id === comandaAtualizada.id
        )

        if (indice >= 0) {
            comandas.value[indice] = comandaAtualizada
        } else {
            comandas.value.push(comandaAtualizada)
        }

        ordenarComandas()
    }

    const tratarErro = (erro, mensagemPadrao) => {
        abrirPopup(
            'Erro',
            erro.response?.data?.mensagem || mensagemPadrao,
            'erro'
        )
    }

    const carregarComandas = async () => {
        carregandoComandas.value = true

        try {
            const { data } = await $api.get('/comandas')

            comandas.value = Array.isArray(data)
                ? data
                : []

            ordenarComandas()
        } catch (erro) {
            comandas.value = []

            tratarErro(
                erro,
                'Não foi possível carregar as comandas.'
            )
        } finally {
            carregandoComandas.value = false
        }
    }

    const criarComanda = async (numero) => {
        try {
            const { data } = await $api.post(
                '/comandas',
                {
                    numero: Number(numero)
                }
            )

            atualizarNaLista(data)

            abrirPopup(
                'Comanda criada',
                `A comanda ${data.numero} foi criada com sucesso.`
            )

            return data
        } catch (erro) {
            tratarErro(
                erro,
                'Não foi possível criar a comanda.'
            )

            return null
        }
    }

    const abrirComanda = async (id) => {
        try {
            const { data } = await $api.patch(
                `/comandas/${id}/abrir`
            )

            atualizarNaLista(data)

            abrirPopup(
                'Comanda aberta',
                `A comanda ${data.numero} está liberada para uso.`
            )

            return data
        } catch (erro) {
            tratarErro(
                erro,
                'Não foi possível abrir a comanda.'
            )

            return null
        }
    }

    const fecharComanda = async (id) => {
        try {
            const { data } = await $api.patch(
                `/comandas/${id}/fechar`
            )

            atualizarNaLista(data)

            abrirPopup(
                'Comanda fechada',
                `A comanda ${data.numero} foi fechada.`
            )

            return data
        } catch (erro) {
            tratarErro(
                erro,
                'Não foi possível fechar a comanda.'
            )

            return null
        }
    }

    const arquivarComanda = async (id) => {
        try {
            await $api.delete(`/comandas/${id}`)

            comandas.value = comandas.value.filter(
                comanda => comanda.id !== id
            )

            abrirPopup(
                'Comanda arquivada',
                'A comanda foi arquivada com sucesso.'
            )

            return true
        } catch (erro) {
            tratarErro(
                erro,
                'Não foi possível arquivar a comanda.'
            )

            return false
        }
    }

    return {
        comandas,
        carregandoComandas,
        carregarComandas,
        criarComanda,
        abrirComanda,
        fecharComanda,
        arquivarComanda
    }
}