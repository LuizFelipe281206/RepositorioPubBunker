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
          Pedido #{{ pedido.id }}
        </template>

        <template #subtitle>
          Status: {{ pedido.status }}
        </template>

        <template #content>
          <p>
            <strong>Total:</strong>
            {{ formatarPreco(pedido.valorTotal) }}
          </p>

          <ul v-if="pedido.produtos?.length">
            <li
                v-for="produto in pedido.produtos"
                :key="produto.id"
            >
              {{ produto.nome }}
            </li>
          </ul>
        </template>

        <template #footer>
          <div class="acoes-card">
            <Button
                label="Em preparo"
                severity="warn"
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
                @click="
                atualizarStatus(
                  pedido.id,
                  'CONCLUIDO'
                )
              "
            />

            <Button
                label="Fechar"
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