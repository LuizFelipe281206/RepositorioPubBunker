export const useComanda = () => {
    const comandaAtual = useState(
        'comanda-atual',
        () => null
    )

    const carregandoComanda = useState(
        'carregando-comanda',
        () => false
    )

    const codigoComanda = computed(
        () => comandaAtual.value?.codigoAcesso || ''
    )

    const comandaAtiva = computed(
        () => comandaAtual.value?.status === 'EM_USO'
    )

    const { $api } = useNuxtApp()
    const { abrirPopup } = usePopup()

    const salvarComanda = (comanda) => {
        comandaAtual.value = comanda

        if (import.meta.client) {
            sessionStorage.setItem(
                'comanda',
                JSON.stringify(comanda)
            )
        }
    }

    const limparComanda = () => {
        comandaAtual.value = null

        if (import.meta.client) {
            sessionStorage.removeItem('comanda')
        }
    }

    const restaurarComanda = () => {
        if (import.meta.server || comandaAtual.value) {
            return
        }

        const comandaSalva =
            sessionStorage.getItem('comanda')

        if (!comandaSalva) {
            return
        }

        try {
            const comanda = JSON.parse(comandaSalva)

            if (comanda.status === 'EM_USO') {
                comandaAtual.value = comanda
            } else {
                limparComanda()
            }
        } catch {
            limparComanda()
        }
    }

    const acessarComanda = async (
        codigoAcesso
    ) => {
        carregandoComanda.value = true

        try {
            const { data } = await $api.get(
                `/comandas/acesso/${
                    encodeURIComponent(codigoAcesso)
                }`
            )

            salvarComanda(data)

            return true
        } catch {
            limparComanda()

            abrirPopup(
                'Comanda indisponível',
                'Esta comanda não existe ou não está liberada para uso.',
                'erro'
            )

            return false
        } finally {
            carregandoComanda.value = false
        }
    }

    const sairComanda = async () => {
        limparComanda()

        await navigateTo('/login')
    }

    return {
        comandaAtual,
        codigoComanda,
        comandaAtiva,
        carregandoComanda,
        restaurarComanda,
        acessarComanda,
        limparComanda,
        sairComanda
    }
}