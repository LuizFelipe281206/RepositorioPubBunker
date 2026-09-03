<script setup>
definePageMeta({
  role: 'ADMIN'
})

const {
  produtos,
  carregarProdutos,
  excluirProduto
} = useProdutos()

const { abrirPopup } = usePopup()

const produtoEditando = ref(null)

onMounted(carregarProdutos)

const excluir = async (id) => {
  try {
    await excluirProduto(id)

    abrirPopup(
        'Produto excluído',
        'O produto foi removido com sucesso.'
    )
  } catch (error) {
    abrirPopup(
        'Erro',
        `Não foi possível excluir o produto. Status: ${
            error.response?.status ||
            'desconhecido'
        }`,
        'erro'
    )
  }
}
</script>

<template>
  <main class="container">
    <AppHeader />

    <AdminOrders class="painel" />
    <AdicionalManager />
    <ProductForm
        :produto="produtoEditando"
        @salvo="produtoEditando = null"
        @cancelado="produtoEditando = null"
    />

    <section>
      <h2>Produtos cadastrados</h2>

      <ProductList
          :produtos="produtos"
          modo-admin
          @editar="produtoEditando = $event"
          @excluir="excluir"
      />
    </section>
  </main>
</template>