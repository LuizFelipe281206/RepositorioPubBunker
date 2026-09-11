export const gerarLinkComanda = (baseUrl, codigoAcesso) => {
    if (!baseUrl || !codigoAcesso) return ''

    const base = `${baseUrl.replace(/\/+$/, '')}/`

    return new URL(
        `comanda/${encodeURIComponent(codigoAcesso)}`,
        base
    ).href
}

export const enderecoApenasLocal = (baseUrl) => {
    if (!baseUrl) return false

    const { hostname } = new URL(baseUrl)

    return hostname === 'localhost' ||
        hostname.endsWith('.localhost') ||
        /^127\./.test(hostname) ||
        ['[::1]', '0.0.0.0', '[::]'].includes(hostname)
}

export const descreverErroComanda = (erro) => {
    const status = erro.response?.status

    if ([400, 401, 404].includes(status)) {
        return {
            titulo: 'Comanda indisponível',
            mensagem: erro.response.data?.mensagem ||
                'Esta comanda não existe ou não está liberada para uso.'
        }
    }

    if (!status || [502, 503, 504].includes(status)) {
        return {
            titulo: 'Falha de conexão',
            mensagem:
                'Não foi possível conectar ao servidor de atendimento. ' +
                'Verifique a conexão e tente novamente.'
        }
    }

    return {
        titulo: 'Não foi possível acessar a comanda',
        mensagem:
            'O servidor não conseguiu concluir a consulta. ' +
            'Tente novamente em instantes.'
    }
}