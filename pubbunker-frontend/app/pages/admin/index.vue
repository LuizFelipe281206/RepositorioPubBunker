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

const produtoParaExcluir = ref(null)
const confirmacaoProdutoVisivel = ref(false)
const excluindoProduto = ref(false)

const solicitarExclusao = (id) => {
  if (excluindoProduto.value) return

  const produto = produtos.value.find(item => item.id === id)
  if (!produto || produto.ativo) return

  produtoParaExcluir.value = produto
  confirmacaoProdutoVisivel.value = true
}

const excluir = async () => {
  if (!produtoParaExcluir.value || excluindoProduto.value) return

  excluindoProduto.value = true

  try {
    await excluirProduto(produtoParaExcluir.value.id)

    confirmacaoProdutoVisivel.value = false

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
  } finally {
    excluindoProduto.value = false
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
  :closable="false"
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
          @excluir="solicitarExclusao"
      />
    </section>
    <Dialog
  v-model:visible="confirmacaoProdutoVisivel"
  modal
  header="Excluir produto"
  :closable="false"
  :draggable="false"
  :close-on-escape="!excluindoProduto"
  :style="{ width: 'min(92vw, 420px)' }"
  @after-hide="produtoParaExcluir = null"
>
  <p v-if="produtoParaExcluir">
    Tem certeza que deseja excluir
    <strong>{{ produtoParaExcluir.nome }}</strong>?
  </p>

  <template #footer>
    <Button
      label="Cancelar"
      severity="secondary"
      text
      :disabled="excluindoProduto"
      @click="confirmacaoProdutoVisivel = false"
    />

    <Button
      label="Excluir"
      severity="danger"
      :loading="excluindoProduto"
      :disabled="excluindoProduto"
      @click="excluir"
    />
  </template>
</Dialog>
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