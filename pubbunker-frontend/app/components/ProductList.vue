<script setup>
defineProps({
  produtos: {
    type: Array,
    required: true
  },

  modoAdmin: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'adicionar',
  'editar',
  'excluir'
])

const dialogQuantidadeVisivel = ref(false)
const produtoSelecionado = ref(null)
const quantidadeSelecionada = ref(1)

const abrirSeletorQuantidade = produto => {
  produtoSelecionado.value = produto
  quantidadeSelecionada.value = 1
  dialogQuantidadeVisivel.value = true
}

const diminuirQuantidade = () => {
  quantidadeSelecionada.value = Math.max(
      1,
      quantidadeSelecionada.value - 1
  )
}

const aumentarQuantidade = () => {
  quantidadeSelecionada.value = Math.min(
      99,
      quantidadeSelecionada.value + 1
  )
}

const confirmarAdicao = () => {
  if (!produtoSelecionado.value) return

  emit(
      'adicionar',
      produtoSelecionado.value,
      quantidadeSelecionada.value
  )

  dialogQuantidadeVisivel.value = false
  produtoSelecionado.value = null
  quantidadeSelecionada.value = 1
}

const totalSelecionado = computed(() =>
    Number(produtoSelecionado.value?.preco || 0) *
    quantidadeSelecionada.value
)

const formatarPreco = valor =>
    Number(valor).toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })
</script>

<template>
  <div class="lista-produtos">
    <Card
        v-for="produto in produtos"
        :key="produto.id"
        class="produto-card"
    >
      <template #title>
        {{ produto.nome }}
      </template>

      <template #content>
        <p>{{ produto.descricao }}</p>

        <p>
          <strong>
            {{ formatarPreco(produto.preco) }}
          </strong>
        </p>

        <Tag
            v-if="produto.categoria"
            :value="produto.categoria"
            severity="secondary"
        />
      </template>

      <template #footer>
        <div class="acoes-card">
          <template v-if="modoAdmin">
            <Button
                label="Editar"
                icon="pi pi-pencil"
                severity="warn"
                @click="emit('editar', produto)"
            />

            <Button
                label="Excluir"
                icon="pi pi-trash"
                severity="danger"
                @click="emit('excluir', produto.id)"
            />
          </template>

          <Button
              v-else
              label="Adicionar ao pedido"
              icon="pi pi-shopping-cart"
              severity="success"
              @click="
                abrirSeletorQuantidade(produto)
              "
          />
        </div>
      </template>
    </Card>
  </div>

  <Dialog
      v-model:visible="dialogQuantidadeVisivel"
      modal
      header="Adicionar ao pedido"
      class="popup-dialog"
      :draggable="false"
  >
    <div
        v-if="produtoSelecionado"
        class="seletor-produto-dialogo"
    >
      <div>
        <h3>{{ produtoSelecionado.nome }}</h3>

        <p class="texto-secundario">
          {{ formatarPreco(produtoSelecionado.preco) }}
          por unidade
        </p>
      </div>

      <div class="controle-quantidade-dialogo">
        <Button
            icon="pi pi-minus"
            severity="secondary"
            outlined
            :disabled="quantidadeSelecionada <= 1"
            aria-label="Diminuir quantidade"
            @click="diminuirQuantidade"
        />

        <strong class="quantidade-dialogo">
          {{ quantidadeSelecionada }}
        </strong>

        <Button
            icon="pi pi-plus"
            severity="secondary"
            outlined
            :disabled="quantidadeSelecionada >= 99"
            aria-label="Aumentar quantidade"
            @click="aumentarQuantidade"
        />
      </div>

      <p class="total-item-dialogo">
        <strong>Total:</strong>
        {{ formatarPreco(totalSelecionado) }}
      </p>
    </div>

    <template #footer>
      <Button
          label="Cancelar"
          severity="secondary"
          text
          @click="
            dialogQuantidadeVisivel = false
          "
      />

      <Button
          label="Adicionar"
          icon="pi pi-shopping-cart"
          severity="success"
          @click="confirmarAdicao"
      />
    </template>
  </Dialog>
</template>