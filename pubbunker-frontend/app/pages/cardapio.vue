<script setup>
definePageMeta({
  role: 'CLIENTE'
})

const {
  produtos,
  carregarProdutos
} = useProdutos()

const { adicionar } = useCarrinho()

onMounted(carregarProdutos)
</script>

<template>
  <main class="container">
    <AppHeader />

    <section>
      <h2>Cardápio</h2>

      <ProductList
          :produtos="produtos"
          @adicionar="adicionar"
      />
    </section>
  </main>
</template>