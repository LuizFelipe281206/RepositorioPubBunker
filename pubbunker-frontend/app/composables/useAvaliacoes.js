export const useAvaliacoes = () => {
    const avaliacoes = useState(
        'avaliacoes',
        () => []
    )

    const carregandoAvaliacoes = useState(
        'carregando-avaliacoes',
        () => false
    )

    const { $api } = useNuxtApp()
    const { usuarioId } = useAuth()
    const { abrirPopup } = usePopup()

    const carregarAvaliacoes = async () => {
        if (!usuarioId.value) return

        carregandoAvaliacoes.value = true

        try {
            const { data } = await $api.get(
                `/avaliacoes/usuario/${usuarioId.value}`
            )

            avaliacoes.value = data
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível carregar suas avaliações.',
                'erro'
            )
        } finally {
            carregandoAvaliacoes.value = false
        }
    }

    const criarAvaliacao = async (dados) => {
        await $api.post('/avaliacoes', {
            usuarioId: usuarioId.value,
            nota: Number(dados.nota),
            comentario: dados.comentario
        })

        await carregarAvaliacoes()
    }

    const atualizarAvaliacao = async (
        id,
        dados
    ) => {
        await $api.put(
            `/avaliacoes/${id}`,
            {
                nota: Number(dados.nota),
                comentario: dados.comentario
            }
        )

        await carregarAvaliacoes()
    }

    const excluirAvaliacao = async (id) => {
        await $api.delete(`/avaliacoes/${id}`)

        await carregarAvaliacoes()
    }

    return {
        avaliacoes,
        carregandoAvaliacoes,
        carregarAvaliacoes,
        criarAvaliacao,
        atualizarAvaliacao,
        excluirAvaliacao
    }
}