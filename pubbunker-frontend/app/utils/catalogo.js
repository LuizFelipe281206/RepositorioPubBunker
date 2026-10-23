export const CATEGORIAS = Object.freeze(['Bebidas', 'Porções', 'Lanches'])
export const LIMITES = Object.freeze({ nome: 100, descricao: 500, observacao: 500,
  produtoCentavos: 100000, adicionalCentavos: 10000, pedidoCentavos: 1000000,
  adicionais: 4, quantidade: 100, itens: 50 })

export const normalizarNome = valor => String(valor ?? '').replace(/\s+/gu, ' ').trim()
export const chaveNome = valor => normalizarNome(valor).toLocaleLowerCase('pt-BR')
export const mensagemErro = (erro, fallback) => {
  const data = erro?.response?.data
  const campos = data && typeof data.campos === 'object' && data.campos
    ? Object.values(data.campos).filter(x => typeof x === 'string' && x.trim()) : []
  if (campos.length) return [...new Set(campos)].join(' ')
  return typeof data?.mensagem === 'string' && data.mensagem.trim()
    ? data.mensagem : fallback
}
export const centavos = valor => {
  if (!['number', 'string'].includes(typeof valor) || String(valor).trim() === '') return null
  const numero = Number(valor)
  const inteiro = Math.round(numero * 100)
  return Number.isFinite(numero) && Number.isSafeInteger(inteiro)
    && Math.abs(numero * 100 - inteiro) < 0.000001 ? inteiro : null
}
export const precoValido = (valor, minimo, maximo) => {
  const numero = centavos(valor)
  return numero !== null && numero >= minimo && numero <= maximo
}
export const mensagemLimitePedido =
  'O total do pedido deve ser no máximo R$ 10.000,00. Remova itens ou reduza as quantidades para continuar.'

export const calcularItemCentavos = item => {
  const base = centavos(item.preco)
  if (base === null || base < 1 || base > LIMITES.produtoCentavos)
    throw new Error('O preço base do produto deve estar entre R$ 0,01 e R$ 1.000,00.')
  if (!Number.isInteger(item.quantidade) || item.quantidade < 1 || item.quantidade > LIMITES.quantidade)
    throw new Error('Escolha de 1 a 100 unidades por item.')
  const extras = item.adicionaisSelecionados || []
  if (!Array.isArray(extras) || extras.length > LIMITES.adicionais)
    throw new Error('Selecione no máximo 4 adicionais por item do pedido.')
  const ids = new Set()
  let unitario = base
  for (const extra of extras) {
    if (!extra || !Number.isSafeInteger(extra.id) || extra.id < 1 || ids.has(extra.id))
      throw new Error('Confira os adicionais selecionados.')
    ids.add(extra.id)
    const valor = centavos(extra.preco)
    if (valor === null || valor < 0 || valor > LIMITES.adicionalCentavos)
      throw new Error('O preço do adicional deve estar entre R$ 0,00 e R$ 100,00.')
    unitario += valor
  }
  return { unitario, subtotal: unitario * item.quantidade }
}
export const validarCarrinho = (itens, observacao = '') => {
  if (!Array.isArray(itens) || !itens.length) return 'Adicione ao menos um produto.'
  if (itens.length > LIMITES.itens) return 'O pedido deve possuir no máximo 50 itens.'
  if (String(observacao ?? '').length > LIMITES.observacao)
    return 'A observação deve possuir no máximo 500 caracteres.'
  try {
    let total = 0
    for (const item of itens) {
      if (!item || !Number.isSafeInteger(item.id) || item.id < 1) return 'Confira os produtos do carrinho.'
      total += calcularItemCentavos(item).subtotal
      if (total > LIMITES.pedidoCentavos) return mensagemLimitePedido
    }
    return null
  } catch (erro) { return erro.message }
}
export const prepararInclusao = (itens, produto, quantidade = 1, adicionais = []) => {
  if (!produto || !Number.isSafeInteger(produto.id) || produto.id < 1 || produto.ativo === false)
    return { erro: 'Este produto não está disponível.' }
  if (!Array.isArray(adicionais)) return { erro: 'Confira os adicionais selecionados.' }
  const ids = [...new Set(adicionais.map(x => x?.id))].sort((a,b) => a-b)
  if (ids.length !== adicionais.length) return { erro: 'Confira os adicionais selecionados.' }
  const chaveConfiguracao = `${produto.id}:${ids.join('-')}`
  const novo = { ...produto, quantidade, adicionaisSelecionados: [...adicionais], chaveConfiguracao }
  const indice = itens.findIndex(x => x.chaveConfiguracao === chaveConfiguracao)
  const lista = itens.map(x => ({ ...x }))
  if (indice >= 0) novo.quantidade += lista[indice].quantidade
  try { novo.precoUnitario = calcularItemCentavos(novo).unitario / 100 }
  catch (e) { return { erro: e.message } }
  if (indice >= 0) lista[indice] = novo
  else lista.push(novo)
  const erro = validarCarrinho(lista)
  return erro ? { erro } : { itens: lista }
}
export const agruparAdicionais = adicionais => [...CATEGORIAS, 'A classificar']
  .map(label => ({ label, items: adicionais.filter(a => (CATEGORIAS.includes(a.categoria) ? a.categoria : 'A classificar') === label) }))
  .filter(grupo => grupo.items.length)
