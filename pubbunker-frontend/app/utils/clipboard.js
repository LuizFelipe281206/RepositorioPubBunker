export const copiarTexto = async (texto) => {
    if (!texto || typeof window === 'undefined') {
        return false
    }

    if (window.isSecureContext && navigator.clipboard?.writeText) {
        try {
            await navigator.clipboard.writeText(texto)
            return true
        } catch {
            // Tenta a cópia por seleção abaixo.
        }
    }

    const focoAnterior = document.activeElement
    const campo = document.createElement('textarea')

    campo.value = texto
    campo.readOnly = true
    campo.style.cssText =
        'position:fixed;top:0;left:0;opacity:0;' +
        'font-size:16px;pointer-events:none;'

    const container =
        focoAnterior?.closest('[role="dialog"]') || document.body

    container.appendChild(campo)

    try {
        campo.focus()
        campo.select()
        campo.setSelectionRange(0, texto.length)

        return document.execCommand?.('copy') === true
    } catch {
        return false
    } finally {
        campo.remove()
        focoAnterior?.focus({ preventScroll: true })
    }
}