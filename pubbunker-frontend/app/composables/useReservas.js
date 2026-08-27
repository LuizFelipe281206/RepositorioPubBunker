export const useReservas = () => {
    const reservas = useState(
        'reservas',
        () => []
    )

    const carregandoReservas = useState(
        'carregando-reservas',
        () => false
    )

    const { $api } = useNuxtApp()
    const { usuarioId } = useAuth()
    const { abrirPopup } = usePopup()

    const carregarReservas = async () => {
        carregandoReservas.value = true

        try {
            const { data } =
                await $api.get('/reservas')

            reservas.value = data
        } catch {
            abrirPopup(
                'Erro',
                'Não foi possível carregar as reservas.',
                'erro'
            )
        } finally {
            carregandoReservas.value = false
        }
    }

    const criarReserva = async (dados) => {
        await $api.post('/reservas', {
            funcionarioId: usuarioId.value,
            nomeCliente: dados.nomeCliente,
            quantidadePessoas:
                Number(dados.quantidadePessoas),
            dataHora: dados.dataHora
        })

        await carregarReservas()
    }

    const atualizarReserva = async (
        id,
        dados
    ) => {
        await $api.put(
            `/reservas/${id}`,
            {
                nomeCliente: dados.nomeCliente,
                quantidadePessoas:
                    Number(dados.quantidadePessoas),
                dataHora: dados.dataHora
            }
        )

        await carregarReservas()
    }

    const atualizarStatusReserva = async (
        id,
        status
    ) => {
        await $api.patch(
            `/reservas/${id}/status`,
            { status }
        )

        await carregarReservas()
    }

    const cancelarReserva = async (id) => {
        await $api.patch(
            `/reservas/${id}/cancelar`
        )

        await carregarReservas()
    }

    const excluirReserva = async (id) => {
        await $api.delete(`/reservas/${id}`)

        await carregarReservas()
    }

    return {
        reservas,
        carregandoReservas,
        carregarReservas,
        criarReserva,
        atualizarReserva,
        atualizarStatusReserva,
        cancelarReserva,
        excluirReserva
    }
}