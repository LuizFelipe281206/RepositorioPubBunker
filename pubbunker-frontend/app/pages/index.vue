<script setup>
const {
  role,
  restaurarSessao
} = useAuth()

onMounted(async () => {
  restaurarSessao()

  await navigateTo(
      role.value === 'ADMIN'
          ? '/admin'
          : '/cardapio',
      {
        replace: true
      }
  )
})
</script>

<template>
  <main class="pagina-centralizada">
    <ProgressSpinner />
  </main>
</template>