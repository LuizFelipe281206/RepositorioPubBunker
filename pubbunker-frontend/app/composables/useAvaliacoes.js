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
    const { usuarioId, role } = useAuth()
    const podeAvaliar = computed(() => Boolean(usuarioId.value) && role.value === 'CLIENTE')
    const validarUsuario = () => {
        if (!podeAvaliar.value) throw new Error('Entre com uma conta de cliente para avaliar.')
    }
    const { abrirPopup } = usePopup()

    const carregarAvaliacoes = async () => {
        if (!podeAvaliar.value) {
            avaliacoes.value = []
            return
        }

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
        validarUsuario()
        await $api.post('/avaliacoes', {
            usuarioId: usuarioId.value,
            nota: Number(dados.nota),
            comentario: (dados.comentario || '').trim() || null
        })

        await carregarAvaliacoes()
    }

    const atualizarAvaliacao = async (
        id,
        dados
    ) => {
        validarUsuario()
        await $api.put(
            `/avaliacoes/${id}`,
            {
                nota: Number(dados.nota),
                comentario: (dados.comentario || '').trim() || null
            }
        )

        await carregarAvaliacoes()
    }

    const excluirAvaliacao = async (id) => {
        validarUsuario()
        await $api.delete(`/avaliacoes/${id}`)

        await carregarAvaliacoes()
    }

    return {
        podeAvaliar,
        avaliacoes,
        carregandoAvaliacoes,
        carregarAvaliacoes,
        criarAvaliacao,
        atualizarAvaliacao,
        excluirAvaliacao
    }
}