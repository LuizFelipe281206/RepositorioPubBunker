<script setup>
const props = defineProps({
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

const categoriasCardapio = [
  'Bebidas',
  'Porções',
  'Lanches'
]

const categoriaSelecionada = ref('Bebidas')

const produtosExibidos = computed(() => {
  if (props.modoAdmin) {
    return props.produtos
  }

  return props.produtos.filter(
      produto =>
          produto.categoria === categoriaSelecionada.value
  )
})

const dialogQuantidadeVisivel = ref(false)
const produtoSelecionado = ref(null)
const quantidadeSelecionada = ref(1)
const adicionaisSelecionadosIds = ref([])

const adicionaisDisponiveis = computed(() => {
  return (
      produtoSelecionado.value
          ?.adicionaisDisponiveis || []
  ).filter(
      adicional => adicional.ativo
  )
})

const adicionaisSelecionados = computed(() => {
  return adicionaisDisponiveis.value.filter(
      adicional =>
          adicionaisSelecionadosIds.value.includes(
              adicional.id
          )
  )
})

const abrirSeletorQuantidade = produto => {
  produtoSelecionado.value = produto
  quantidadeSelecionada.value = 1
  adicionaisSelecionadosIds.value = []
  dialogQuantidadeVisivel.value = true
}

const limparSeletor = () => {
  produtoSelecionado.value = null
  quantidadeSelecionada.value = 1
  adicionaisSelecionadosIds.value = []
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
  if (!produtoSelecionado.value) {
    return
  }

  emit(
      'adicionar',
      produtoSelecionado.value,
      quantidadeSelecionada.value,
      adicionaisSelecionados.value
  )

  dialogQuantidadeVisivel.value = false
}

const valorAdicionais = computed(() => {
  return adicionaisSelecionados.value.reduce(
      (soma, adicional) =>
          soma + Number(adicional.preco),
      0
  )
})

const precoUnitarioSelecionado = computed(() => {
  return (
      Number(
          produtoSelecionado.value?.preco || 0
      ) +
      valorAdicionais.value
  )
})

const totalSelecionado = computed(() => {
  return (
      precoUnitarioSelecionado.value *
      quantidadeSelecionada.value
  )
})

const formatarPreco = valor =>
    Number(valor).toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })
</script>

<template>
  <nav
    v-if="!modoAdmin"
    class="abas-cardapio"
    aria-label="Categorias do cardápio"
>
  <button
      v-for="categoria in categoriasCardapio"
      :key="categoria"
      type="button"
      class="aba-cardapio"
      :class="{
        'aba-cardapio-ativa':
            categoriaSelecionada === categoria
      }"
      :aria-selected="
        categoriaSelecionada === categoria
      "
      role="tab"
      @click="categoriaSelecionada = categoria"
  >
    {{ categoria }}
  </button>
</nav>

<p
    v-if="!produtosExibidos.length"
    class="categoria-vazia"
>
  Nenhum produto disponível nesta categoria.
</p>
  <div class="lista-produtos">
    <Card
        v-for="produto in produtosExibidos"        
        :key="produto.id"
        class="produto-card"
    >
      <template #title>
        {{ produto.nome }}
      </template>

      <template #content>
        <div class="produto-conteudo">
          <p class="produto-descricao">
            {{ produto.descricao }}
          </p>

          <div class="produto-detalhes">
            <strong class="produto-preco">
              {{ formatarPreco(produto.preco) }}
            </strong>

            <Tag
                v-if="produto.categoria"
                :value="produto.categoria"
                severity="secondary"
                class="produto-categoria"
            />
          </div>
        </div>
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
      @hide="limparSeletor"
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

      <div
          v-if="adicionaisDisponiveis.length"
          class="adicionais-dialogo"
      >
        <h4>Adicionais</h4>

        <label
            v-for="adicional in adicionaisDisponiveis"
            :key="adicional.id"
            :for="`adicional-${adicional.id}`"
            class="adicional-dialogo-item"
        >
          <Checkbox
              v-model="adicionaisSelecionadosIds"
              :input-id="`adicional-${adicional.id}`"
              :value="adicional.id"
          />

          <span class="adicional-dialogo-dados">
            <strong>{{ adicional.nome }}</strong>

            <small>
              + {{ formatarPreco(adicional.preco) }}
              por unidade
            </small>
          </span>
        </label>
      </div>

      <div class="quantidade-dialogo-bloco">
        <span>Quantidade</span>

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
      </div>

      <div class="resumo-item-dialogo">
        <span>
          Valor por unidade:
          <strong>
            {{
              formatarPreco(
                  precoUnitarioSelecionado
              )
            }}
          </strong>
        </span>

        <span class="total-item-dialogo">
          Total:
          <strong>
            {{ formatarPreco(totalSelecionado) }}
          </strong>
        </span>
      </div>
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

<style scoped>
.adicionais-dialogo {
  display: grid;
  gap: 8px;
  padding: 14px 0;
  border-top: 1px solid var(--bunker-border);
  border-bottom: 1px solid var(--bunker-border);
}

.adicionais-dialogo h4 {
  margin: 0 0 4px;
}

.adicional-dialogo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  cursor: pointer;
  background: var(--bunker-bg);
  border: 1px solid var(--bunker-border);
  border-radius: 7px;
}

.adicional-dialogo-dados {
  display: flex;
  flex: 1;
  justify-content: space-between;
  gap: 12px;
}

.adicional-dialogo-dados small {
  color: var(--bunker-muted);
}

.quantidade-dialogo-bloco {
  display: grid;
  gap: 10px;
}

.resumo-item-dialogo {
  display: grid;
  gap: 6px;
  padding: 12px;
  background: var(--bunker-bg);
  border-radius: 7px;
}

.total-item-dialogo {
  color: var(--bunker-wine);
}

@media (max-width: 480px) {
  .adicional-dialogo-dados {
    flex-direction: column;
    gap: 3px;
  }
}
</style>