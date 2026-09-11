<script setup>
definePageMeta({ roles: ['ADMIN', 'FUNCIONARIO'] })
</script>

<template>
  <main class="container">
    <AppHeader />
    <AdminOrders class="painel" />
  </main>
</template>