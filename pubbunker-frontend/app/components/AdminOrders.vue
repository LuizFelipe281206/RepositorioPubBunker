<script setup>
const {
  pedidos,
  carregarPedidos,
  atualizarStatus,
  fecharPedido
} = usePedidos()

onMounted(carregarPedidos)

const formatarPreco = valor =>
    Number(valor).toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })

const formatarData = valor => {
  if (!valor) return ''

  return new Date(valor).toLocaleString('pt-BR')
}
</script>

<template>
  <section>
    <h2>Pedidos recebidos</h2>

    <p
        v-if="pedidos.length === 0"
        class="texto-secundario"
    >
      Nenhum pedido recebido.
    </p>

    <div class="grade-pedidos">
      <Card
          v-for="pedido in pedidos"
          :key="pedido.id"
      >
        <template #title>
          <span v-if="pedido.numeroComanda">
            Comanda {{ pedido.numeroComanda }}
            — Pedido #{{ pedido.id }}
          </span>

          <span v-else>
            Pedido #{{ pedido.id }}
          </span>
        </template>

        <template #subtitle>
          <span v-if="pedido.numeroComanda">
            Pedido realizado pela comanda
          </span>

          <span v-else>
            Cliente:
            {{ pedido.clienteNome || 'Não identificado' }}
          </span>

          <br>

          Status: {{ pedido.status }}

          <span v-if="pedido.dataPedido">
            — {{ formatarData(pedido.dataPedido) }}
          </span>
        </template>

        <template #content>
          <div class="detalhes-pedido">
            <ul
                v-if="pedido.itens?.length"
                class="lista-itens-pedido"
            >
              <li
                  v-for="item in pedido.itens"
                  :key="item.id"
              >
                <strong>
                  {{ item.quantidade }}x
                  {{ item.produtoNome }}
                </strong>

                — {{ formatarPreco(item.precoUnitario) }}
                cada

                — Subtotal:
                {{ formatarPreco(item.subtotal) }}
              </li>
            </ul>

            <div
                v-if="pedido.observacao"
                class="observacao-pedido"
            >
              <strong>Observação:</strong>

              <p>{{ pedido.observacao }}</p>
            </div>

            <p class="total-pedido">
              <strong>Total:</strong>
              {{ formatarPreco(pedido.valorTotal) }}
            </p>
          </div>
        </template>

        <template #footer>
          <div class="acoes-card">
            <Button
                label="Em preparo"
                severity="warn"
                :disabled="
                  pedido.status === 'CONCLUIDO' ||
                  pedido.status === 'CANCELADO'
                "
                @click="
                  atualizarStatus(
                    pedido.id,
                    'EM_PREPARO'
                  )
                "
            />

            <Button
                label="Concluir"
                severity="success"
                :disabled="
                  pedido.status === 'CONCLUIDO' ||
                  pedido.status === 'CANCELADO'
                "
                @click="
                  atualizarStatus(
                    pedido.id,
                    'CONCLUIDO'
                  )
                "
            />

            <Button
                label="Arquivar"
                severity="secondary"
                :disabled="
                  pedido.status !== 'CONCLUIDO'
                "
                @click="fecharPedido(pedido)"
            />
          </div>
        </template>
      </Card>
    </div>
  </section>
</template>