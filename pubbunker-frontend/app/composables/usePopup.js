export const usePopup = () => {
    const popup = useState('popup', () => ({
        mostrar: false,
        titulo: '',
        mensagem: '',
        tipo: 'sucesso'
    }))

    const abrirPopup = (
        titulo,
        mensagem,
        tipo = 'sucesso'
    ) => {
        popup.value = {
            mostrar: true,
            titulo,
            mensagem,
            tipo
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