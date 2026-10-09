<script setup>
definePageMeta({
  role: 'ADMIN'
})
</script>

<template>
  <main class="container">
    <AppHeader />

    <h2>Cadastros</h2>

    <ProductForm />

    <AdicionalManager />
  </main>
</template>