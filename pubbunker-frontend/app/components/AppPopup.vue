<script setup>
const { popup, fecharPopup } = usePopup()
</script>

<template>
  <Dialog
      v-model:visible="popup.mostrar"
      modal
      :header="popup.titulo"
      :closable="false"
      class="popup-dialog"
  >
    <p>{{ popup.mensagem }}</p>

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