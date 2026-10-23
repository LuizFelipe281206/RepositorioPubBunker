export const resolverBaseApi = (configurado, origem) => {
  if (typeof configurado === 'string' && configurado.trim()) return configurado.trim().replace(/\/+$/, '')
  const endereco = new URL(origem)
  endereco.port = '8080'
  endereco.pathname = '/'
  endereco.search = ''
  endereco.hash = ''
  return endereco.origin
}
