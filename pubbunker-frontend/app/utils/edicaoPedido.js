import { LIMITES, centavos, calcularItemCentavos, agruparAdicionais, mensagemLimitePedido } from './catalogo.js'

export const criarRascunhoPedido = pedido => ({
  revisao: pedido.revisao,
  observacao: pedido.observacao || '',
  itens: (pedido.itens || []).map(item => ({
    chave: `item-${item.id}`, itemId: item.id, produtoId: item.produtoId,
    nome: item.produtoNome, quantidade: item.quantidade,
    precoOriginal: item.precoUnitario,
    extrasOriginais: (item.adicionais || []).map(extra => ({ ...extra })),
    adicionaisIds: (item.adicionais || []).map(extra => extra.id)
  }))
})

export const configuracaoMantida = item => Boolean(item.itemId)
  && item.adicionaisIds.length === item.extrasOriginais.length
  && new Set(item.adicionaisIds).size === item.adicionaisIds.length
  && item.extrasOriginais.every(extra => item.adicionaisIds.includes(extra.id))

export const opcoesAdicionaisEdicao = (item, produtos) => {
  const atuais = produtos.find(produto => produto.id === item.produtoId)?.adicionaisDisponiveis?.filter(extra => extra.ativo) || []
  const extras = [...atuais]
  for (const extra of item.extrasOriginais) {
    if (!extras.some(atual => atual.id === extra.id)) extras.push({ ...extra, indisponivel: true })
  }
  return agruparAdicionais(extras)
}

export const calcularLinhaEdicao = (item, produtos) => {
  if (!Number.isInteger(item.quantidade) || item.quantidade < 1 || item.quantidade > LIMITES.quantidade)
    throw new Error('Escolha de 1 a 100 unidades por item.')
  if (!Array.isArray(item.adicionaisIds) || item.adicionaisIds.length > LIMITES.adicionais
      || new Set(item.adicionaisIds).size !== item.adicionaisIds.length)
    throw new Error('Escolha até 4 adicionais diferentes por item do pedido.')
  if (configuracaoMantida(item)) {
    const unitario = centavos(item.precoOriginal)
    if (unitario === null || unitario < 1) throw new Error('Reabra a edição para conferir o preço deste item.')
    return { unitario, subtotal: unitario * item.quantidade }
  }
  const produto = produtos.find(produto => produto.id === item.produtoId && produto.ativo)
  if (!produto) throw new Error('Este produto não está mais disponível. Remova-o ou escolha outro produto.')
  const extras = item.adicionaisIds.map(id => produto.adicionaisDisponiveis?.find(extra => extra.id === id && extra.ativo))
  if (extras.some(extra => !extra)) throw new Error('Um dos adicionais não está mais disponível para este produto.')
  return calcularItemCentavos({ ...produto, quantidade: item.quantidade, adicionaisSelecionados: extras })
}

export const conferirEdicaoPedido = (rascunho, produtos) => {
  if (!rascunho.itens.length) return { erro: 'Mantenha ao menos um produto no pedido.', total: 0 }
  if (rascunho.itens.length > LIMITES.itens) return { erro: 'O pedido deve possuir no máximo 50 itens.', total: 0 }
  if (rascunho.observacao.length > LIMITES.observacao) return { erro: 'A observação deve possuir no máximo 500 caracteres.', total: 0 }
  try {
    const total = rascunho.itens.reduce((soma, item) => soma + calcularLinhaEdicao(item, produtos).subtotal, 0)
    return { total, erro: total > LIMITES.pedidoCentavos ? mensagemLimitePedido : '' }
  } catch (erro) { return { total: null, erro: erro.message } }
}

export const dadosEdicaoPedido = rascunho => ({
  revisao: rascunho.revisao, observacao: rascunho.observacao.trim() || null,
  itens: rascunho.itens.map(item => ({
    itemId: item.itemId || null, produtoId: item.produtoId,
    quantidade: item.quantidade, adicionaisIds: [...item.adicionaisIds]
  }))
})
