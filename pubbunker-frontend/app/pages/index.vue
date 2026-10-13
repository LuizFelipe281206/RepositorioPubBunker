<script setup>
const {
  role,
  restaurarSessao,
  rotaInicialPorRole
} = useAuth()

onMounted(async () => {
  await restaurarSessao()

  await navigateTo(
    rotaInicialPorRole(role.value),
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