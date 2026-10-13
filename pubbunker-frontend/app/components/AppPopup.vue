<script setup>
const { popup, fecharPopup } = usePopup()

const agora = ref(Date.now())
let intervalo = null

const segundosRestantes = computed(() => {
  if (!popup.value.aguardeAte) return 0

  return Math.max(
    0,
    Math.ceil((popup.value.aguardeAte - agora.value) / 1000)
  )
})

const mensagemExibida = computed(() => {
  if (!popup.value.aguardeAte) {
    return popup.value.mensagem
  }

  if (segundosRestantes.value === 0) {
    return '0 segundos. Você já pode tentar novamente.'
  }

  return `Muitas tentativas. Aguarde ${segundosRestantes.value} segundos e tente novamente.`
})

const pararContador = () => {
  if (intervalo !== null) {
    clearInterval(intervalo)
    intervalo = null
  }
}

const atualizarContador = () => {
  agora.value = Date.now()

  if (segundosRestantes.value === 0) {
    pararContador()
  }
}

watch(
  () => [popup.value.mostrar, popup.value.aguardeAte],
  ([mostrar, aguardeAte]) => {
    pararContador()
    atualizarContador()

    if (
      import.meta.client &&
      mostrar &&
      aguardeAte &&
      segundosRestantes.value > 0
    ) {
      intervalo = setInterval(atualizarContador, 1000)
    }
  },
  { immediate: true }
)

onUnmounted(pararContador)
</script>
<template>
  <Dialog
      v-model:visible="popup.mostrar"
      modal
      :header="popup.titulo"
      :closable="false"
      class="popup-dialog"
  >
    <p>{{ mensagemExibida }}</p>

    <template #footer>
      <Button
          label="OK"
          :severity="
          popup.tipo === 'erro'
            ? 'danger'
            : 'success'
        "
          @click="fecharPopup"
      />
    </template>
  </Dialog>
</template>