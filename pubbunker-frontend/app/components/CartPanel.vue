<script setup>
import { calcularItemCentavos } from "~/utils/catalogo.js"
const {
  carrinho,
  enviandoPedido,
  observacao,
  remover,
  cancelar,
  finalizar
} = useCarrinho()

const confirmacaoCancelamentoVisivel = ref(false)

const solicitarCancelamento = () => {
  if (!carrinho.value.length || enviandoPedido.value) return

  confirmacaoCancelamentoVisivel.value = true
}

const confirmarCancelamento = () => {
  if (enviandoPedido.value) return

  cancelar()
  confirmacaoCancelamentoVisivel.value = false
}

const precoUnitarioItem = item => {
  try { return calcularItemCentavos(item).unitario / 100 } catch { return 0 }
}
const subtotalItem = item => {
  try { return calcularItemCentavos(item).subtotal / 100 } catch { return 0 }
}
const total = computed(() => carrinho.value.reduce((soma, item) => soma + Math.round(subtotalItem(item)*100), 0) / 100)

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

      <div class="itens-carrinho setor-rolavel" role="region" aria-label="Itens do carrinho" tabindex="0">
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
            :disabled="enviandoPedido"
            @click="remover(index)"
        />
      </div>

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
            :disabled="enviandoPedido"
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
            :disabled="!carrinho.length || enviandoPedido"
            :loading="enviandoPedido"
            @click="finalizar"
        />

        <Button
            label="Cancelar"
            severity="secondary"
            :disabled="!carrinho.length || enviandoPedido"
            @click="solicitarCancelamento"
        />
      </div>
    </template>
  </Card>
  <Dialog
  v-model:visible="confirmacaoCancelamentoVisivel"
  modal
  header="Limpar carrinho"
  :closable="false"
  :draggable="false"
  :close-on-escape="!enviandoPedido"
  :style="{ width: 'min(92vw, 420px)' }"
>
  <p>Tem certeza que deseja limpar o carrinho?</p>

  <p class="texto-secundario">
    Serão removidos todos os itens e a observação do carrinho atual.
  </p>

  <template #footer>
    <Button
      label="Voltar"
      severity="secondary"
      text
      :disabled="enviandoPedido"
      @click="confirmacaoCancelamentoVisivel = false"
    />

    <Button
      label="Limpar carrinho"
      severity="danger"
      :disabled="enviandoPedido"
      @click="confirmarCancelamento"
    />
  </template>
</Dialog>
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