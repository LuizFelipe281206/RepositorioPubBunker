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

const edicaoProdutoVisivel = ref(false)

const editarProduto = (produto) => {
  produtoEditando.value = produto
  edicaoProdutoVisivel.value = true
}

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
        error.response?.data?.mensagem ||
            'Não foi possível excluir o produto.',
        'erro'
    )
  }
}
</script>

<template>
  <main class="container">
    <AppHeader />

    <section class="secao-adicionais">
  <AdicionalManager
    :mostrar-formulario="false"
    mostrar-lista
  />
</section>
    <Dialog
  v-model:visible="edicaoProdutoVisivel"
  modal
  header="Editar produto"
  class="produto-edicao-dialog"
  :draggable="false"
  :style="{ width: 'min(92vw, 520px)' }"
  @after-hide="produtoEditando = null"
>
  <ProductForm
    v-if="produtoEditando"
    :produto="produtoEditando"
    @salvo="edicaoProdutoVisivel = false"
    @cancelado="edicaoProdutoVisivel = false"
  />
</Dialog>

    <section>
      <h2>Produtos cadastrados</h2>

      <ProductList
          :produtos="produtos"
          modo-admin
          @editar="editarProduto"
          @excluir="excluir"
      />
    </section>
  </main>
</template>
<style scoped>
.secao-adicionais {
  margin-top: 32px;
}
.produto-edicao-dialog :deep(.painel) {
  margin-bottom: 0;
  border: none;
  box-shadow: none;
}

.produto-edicao-dialog :deep(.p-card-body) {
  padding: 0;
}

.produto-edicao-dialog :deep(.p-card-title) {
  display: none;
}
</style>