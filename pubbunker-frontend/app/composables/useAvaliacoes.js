import { mensagemErro } from '~/utils/catalogo.js'

export const useAvaliacoes = () => {
    const avaliacoes = useState('avaliacoes', () => [])
    const carregandoAvaliacoes = useState('carregando-avaliacoes', () => false)
    const versaoConsulta = useState('versao-consulta-avaliacoes', () => 0)

    const { $api } = useNuxtApp()
    const { codigoComanda, comandaAtiva, tratarAcessoInvalido } = useComanda()
    const { abrirPopup } = usePopup()

    const podeAvaliar = computed(
        () => comandaAtiva.value && Boolean(codigoComanda.value)
    )

    const validarComanda = () => {
        if (!podeAvaliar.value) {
            throw new Error('Acesse uma comanda aberta para avaliar.')
        }

        return codigoComanda.value
    }

    const urlComanda = codigo =>
        `/avaliacoes/comanda/${encodeURIComponent(codigo)}`

    const dadosAvaliacao = dados => ({
        nota: Number(dados.nota),
        nomeCliente: (dados.nomeCliente || '').trim() || null,
        comentario: (dados.comentario || '').trim() || null
    })

    const registrarAlteracao = () => {
        versaoConsulta.value++
        carregandoAvaliacoes.value = false
    }

    const carregarAvaliacoes = async () => {
        const versao = ++versaoConsulta.value

        if (!podeAvaliar.value) {
            avaliacoes.value = []
            carregandoAvaliacoes.value = false
            return
        }

        const codigo = codigoComanda.value
        carregandoAvaliacoes.value = true

        try {
            const { data } = await $api.get(urlComanda(codigo))

            if (
                versao === versaoConsulta.value &&
                codigo === codigoComanda.value
            ) {
                avaliacoes.value = data
            }
        } catch (erro) {
            if (
                versao !== versaoConsulta.value ||
                codigo !== codigoComanda.value
            ) return

            avaliacoes.value = []

            if (!tratarAcessoInvalido(erro, codigo)) {
                abrirPopup(
                    'Não foi possível carregar as avaliações',
                    mensagemErro(erro, 'Não foi possível carregar as avaliações.'),
                    'erro',
                    erro.response?.status === 429 ? 60 : null
                )
            }
        } finally {
            if (versao === versaoConsulta.value) {
                carregandoAvaliacoes.value = false
            }
        }
    }

    const criarAvaliacao = async dados => {
        const codigo = validarComanda()

        try {
            const { data } = await $api.post(
                urlComanda(codigo),
                dadosAvaliacao(dados)
            )

            if (codigo !== codigoComanda.value) return false

            registrarAlteracao()
            avaliacoes.value = [data, ...avaliacoes.value]
            return true
        } catch (erro) {
            if (codigo !== codigoComanda.value) return false
            if (tratarAcessoInvalido(erro, codigo)) return false
            throw erro
        }
    }

    const atualizarAvaliacao = async (id, dados) => {
        const codigo = validarComanda()

        try {
            const { data } = await $api.put(
                `${urlComanda(codigo)}/${id}`,
                dadosAvaliacao(dados)
            )

            if (codigo !== codigoComanda.value) return false

            registrarAlteracao()
            avaliacoes.value = avaliacoes.value.map(
                item => item.id === id ? data : item
            )
            return true
        } catch (erro) {
            if (codigo !== codigoComanda.value) return false
            if (tratarAcessoInvalido(erro, codigo)) return false
            throw erro
        }
    }

    const excluirAvaliacao = async id => {
        const codigo = validarComanda()

        try {
            await $api.delete(`${urlComanda(codigo)}/${id}`)

            if (codigo !== codigoComanda.value) return false

            registrarAlteracao()
            avaliacoes.value = avaliacoes.value.filter(item => item.id !== id)
            return true
        } catch (erro) {
            if (codigo !== codigoComanda.value) return false
            if (tratarAcessoInvalido(erro, codigo)) return false
            throw erro
        }
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