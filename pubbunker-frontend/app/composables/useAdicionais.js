export const useAdicionais = () => {
    const adicionais = useState(
        'adicionais',
        () => []
    )

    const carregandoAdicionais = useState(
        'carregando-adicionais',
        () => false
    )

    const { $api } = useNuxtApp()

    const ordenarAdicionais = () => {
        adicionais.value.sort(
            (a, b) =>
                a.nome.localeCompare(
                    b.nome,
                    'pt-BR'
                )
        )
    }

    const carregarAdicionais = async () => {
        carregandoAdicionais.value = true

        try {
            const { data } =
                await $api.get('/adicionais')

            adicionais.value =
                Array.isArray(data) ? data : []

            ordenarAdicionais()
        } finally {
            carregandoAdicionais.value = false
        }
    }

    const salvarAdicional = async (
        adicional,
        id = null
    ) => {
        const requisicao = id
            ? $api.put(
                `/adicionais/${id}`,
                adicional
            )
            : $api.post(
                '/adicionais',
                adicional
            )

        const { data } = await requisicao

        const indice = adicionais.value.findIndex(
            item => item.id === data.id
        )

        if (indice >= 0) {
            adicionais.value[indice] = data
        } else {
            adicionais.value.push(data)
        }

        ordenarAdicionais()

        return data
    }

    const excluirAdicional = async (id) => {
        await $api.delete(`/adicionais/${id}`)

        adicionais.value =
            adicionais.value.filter(
                adicional => adicional.id !== id
            )
    }

    return {
        adicionais,
        carregandoAdicionais,
        carregarAdicionais,
        salvarAdicional,
        excluirAdicional
    }
}