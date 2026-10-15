export const usePedidos = () => {
  const pedidos = ref([])
  const carregandoPedidos = ref(false)
  const processandoPedidos = ref(false)
  const pedidoProcessandoId = ref(null)
  const erroPedidos = ref('')

  const { $api } = useNuxtApp()
  const { abrirPopup } = usePopup()
  const { usuarioId, role } = useAuth()

  let ativo = true
  let versao = 0

  const identidade = () =>
    usuarioId.value && ['ADMIN', 'FUNCIONARIO'].includes(role.value)
      ? String(usuarioId.value) + ':' + role.value
      : null

  const limpar = () => {
    versao++
    pedidos.value = []
    carregandoPedidos.value = false
    processandoPedidos.value = false
    pedidoProcessandoId.value = null
    erroPedidos.value = ''
  }

  const mostrarErro = (erro, mensagem) => {
    abrirPopup(
      'Erro',
      erro.response?.data?.mensagem || mensagem,
      'erro',
      erro.response?.status === 429 ? 60 : null
    )
  }

  const carregarPedidos = async (silencioso = false) => {
    if (!ativo || processandoPedidos.value) return false

    const autor = identidade()
    if (!autor) {
      limpar()
      return false
    }

    const pedido = ++versao
    if (!silencioso) carregandoPedidos.value = true

    const respostaAtual = () =>
      ativo && pedido === versao && autor === identidade()

    try {
      const { data } = await $api.get('/pedidos')
      if (!respostaAtual()) return false

      pedidos.value = Array.isArray(data) ? data : []
      erroPedidos.value = ''
      return true
    } catch (erro) {
      if (!respostaAtual()) return false

      erroPedidos.value = erro.response?.data?.mensagem ||
        'Não foi possível atualizar os pedidos.'

      if (!silencioso) mostrarErro(erro, erroPedidos.value)
      return false
    } finally {
      if (pedido === versao) carregandoPedidos.value = false
    }
  }

  const alterar = async (id, executar, aplicar, sucesso, mensagemErro) => {
    if (!ativo || processandoPedidos.value) return null

    const autor = identidade()
    if (!autor) return null

    const pedido = ++versao
    carregandoPedidos.value = false
    processandoPedidos.value = true
    pedidoProcessandoId.value = id

    const respostaAtual = () =>
      ativo && pedido === versao && autor === identidade()

    try {
      const resposta = await executar()
      if (!respostaAtual()) return null

      aplicar(resposta.data)
      abrirPopup('Sucesso', sucesso)
      return resposta
    } catch (erro) {
      if (respostaAtual()) mostrarErro(erro, mensagemErro)
      return null
    } finally {
      if (pedido === versao) {
        processandoPedidos.value = false
        pedidoProcessandoId.value = null
      }
    }
  }

  const atualizarStatus = async (id, status) => {
    const pedido = pedidos.value.find(item => item.id === id)
    const proximoStatus = {
      PENDENTE: 'EM_PREPARO',
      EM_PREPARO: 'CONCLUIDO'
    }

    if (!pedido || proximoStatus[pedido.status] !== status) return null

    const resposta = await alterar(
      id,
      () => $api.patch('/pedidos/status/' + encodeURIComponent(id), { status }),
      atualizado => {
        pedidos.value = pedidos.value.map(item =>
          item.id === id ? atualizado : item
        )
      },
      'O status do pedido foi atualizado.',
      'Não foi possível atualizar o status.'
    )

    return resposta?.data || null
  }

  const fecharPedido = async pedido => {
    const atual = pedidos.value.find(item => item.id === pedido?.id)
    if (!atual || atual.status !== 'CONCLUIDO') return false

    const resposta = await alterar(
      atual.id,
      () => $api.delete('/pedidos/' + encodeURIComponent(atual.id)),
      () => {
        pedidos.value = pedidos.value.filter(item => item.id !== atual.id)
      },
      'O pedido foi arquivado.',
      'Não foi possível arquivar o pedido.'
    )

    return Boolean(resposta)
  }

  watch([usuarioId, role], limpar, { flush: 'sync' })

  onBeforeUnmount(() => {
    ativo = false
    limpar()
  })

  return {
    pedidos,
    carregandoPedidos,
    processandoPedidos,
    pedidoProcessandoId,
    erroPedidos,
    carregarPedidos,
    atualizarStatus,
    fecharPedido
  }
}