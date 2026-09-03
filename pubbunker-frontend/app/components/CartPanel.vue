<script setup>
const {
  carrinho,
  observacao,
  remover,
  cancelar,
  finalizar
} = useCarrinho()

const precoUnitarioItem = item => {
  return Number(
      item.precoUnitario ?? item.preco ?? 0
  )
}

const subtotalItem = item => {
  return (
      precoUnitarioItem(item) *
      Number(item.quantidade || 1)
  )
}

const total = computed(() =>
    carrinho.value.reduce(
        (soma, item) =>
            soma + subtotalItem(item),
        0
    )
)

const formatarPreco = valor =>
    Number(valor).toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })
</script>

<template>
  <Card class="painel">
    <template #title>
      Seu pedido
    </template>

    <template #content>
      <p
          v-if="carrinho.length === 0"
          class="texto-secundario"
      >
        O carrinho está vazio.
      </p>

      <div
          v-for="(item, index) in carrinho"
          :key="
            item.chaveConfiguracao ||
            `${item.id}-${index}`
          "
          class="item-carrinho"
      >
        <div class="item-carrinho-conteudo">
          <div class="item-carrinho-cabecalho">
            <strong>
              {{ item.quantidade }}x
              {{ item.nome }}
            </strong>

            <span>
              {{ formatarPreco(subtotalItem(item)) }}
            </span>
          </div>

          <ul
              v-if="
                item.adicionaisSelecionados?.length
              "
              class="lista-adicionais-carrinho"
          >
            <li
                v-for="
                  adicional in
                  item.adicionaisSelecionados
                "
                :key="adicional.id"
            >
              + {{ adicional.nome }}

              <span>
                {{
                  formatarPreco(adicional.preco)
                }}
                por unidade
              </span>
            </li>
          </ul>

          <small class="preco-unitario-carrinho">
            Valor por unidade:
            {{ formatarPreco(precoUnitarioItem(item)) }}
          </small>
        </div>

        <Button
            icon="pi pi-trash"
            severity="danger"
            text
            rounded
            aria-label="Remover item"
            @click="remover(index)"
        />
      </div>

      <div
          v-if="carrinho.length"
          class="campo campo-observacao-pedido"
      >
        <label for="observacao-pedido">
          Observação
        </label>

        <Textarea
            id="observacao-pedido"
            v-model="observacao"
            rows="3"
            maxlength="500"
            auto-resize
            fluid
            placeholder="Ex.: sem cebola, molho separado..."
        />

        <small class="texto-secundario">
          {{ observacao.length }}/500 caracteres
        </small>
      </div>

      <p
          v-if="carrinho.length"
          class="total-pedido"
      >
        <strong>Total:</strong>
        {{ formatarPreco(total) }}
      </p>
    </template>

    <template #footer>
      <div class="acoes-card">
        <Button
            label="Finalizar pedido"
            icon="pi pi-check"
            severity="success"
            :disabled="!carrinho.length"
            @click="finalizar"
        />

        <Button
            label="Cancelar"
            severity="secondary"
            :disabled="!carrinho.length"
            @click="cancelar"
        />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.item-carrinho-conteudo {
  display: grid;
  flex: 1;
  gap: 7px;
  min-width: 0;
}

.item-carrinho-cabecalho {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.item-carrinho-cabecalho span {
  white-space: nowrap;
  color: var(--bunker-wine);
  font-weight: 700;
}

.lista-adicionais-carrinho {
  display: grid;
  gap: 4px;
  margin: 0;
  padding-left: 18px;
  color: var(--bunker-muted);
}

.lista-adicionais-carrinho li span {
  margin-left: 5px;
  font-size: 0.85rem;
}

.preco-unitario-carrinho {
  color: var(--bunker-muted);
}

@media (max-width: 480px) {
  .item-carrinho-cabecalho {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }

  .lista-adicionais-carrinho li span {
    display: block;
    margin-left: 0;
  }
}
</style>