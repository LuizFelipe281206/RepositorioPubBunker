export const usePopup = () => {
    const popup = useState('popup', () => ({
        mostrar: false,
        titulo: '',
        mensagem: '',
        tipo: 'sucesso',
        aguardeAte: null
    }))

    const abrirPopup = (
        titulo,
        mensagem,
        tipo = 'sucesso',
        aguardeSegundos = null
    ) => {
        const segundos = Number(aguardeSegundos)
        const fim = Date.now() + segundos * 1000

        popup.value = {
            mostrar: true,
            titulo,
            mensagem,
            tipo,
            aguardeAte:
                segundos > 0 && Number.isFinite(fim)
                    ? fim
                    : null
        }
    }

    const fecharPopup = () => {
        popup.value.mostrar = false
    }

    return {
        popup,
        abrirPopup,
        fecharPopup
    }
}