<script setup>
const {
  carrinho,
  observacao,
  remover,
  cancelar,
  finalizar
} = useCarrinho()

const total = computed(() =>
    carrinho.value.reduce(
        (soma, item) =>
            soma +
            Number(item.preco) *
            Number(item.quantidade || 1),
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
          :key="item.id"
          class="item-carrinho"
      >
        <span>
          {{ item.quantidade }}x
          {{ item.nome }} —
          {{
            formatarPreco(
                Number(item.preco) *
                Number(item.quantidade)
            )
          }}
        </span>

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