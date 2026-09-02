<script setup>
definePageMeta({
  role: 'CLIENTE'
})

const {
  comandaAtual
} = useComanda()

const {
  pedidosComanda,
  carregandoPedidosComanda,
  carregarPedidosComanda
} = usePedidosComanda()

let intervaloAtualizacao = null

onMounted(async () => {
  await carregarPedidosComanda()

  intervaloAtualizacao = setInterval(
      () => carregarPedidosComanda(true),
      10000
  )
})

onBeforeUnmount(() => {
  if (intervaloAtualizacao) {
    clearInterval(intervaloAtualizacao)
  }
})

const formatarPreco = valor =>
    Number(valor).toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })

const formatarData = valor => {
  if (!valor) return ''

  return new Date(valor).toLocaleString('pt-BR')
}

const dadosStatus = status => {
  const configuracoes = {
    PENDENTE: {
      texto: 'Pendente',
      severity: 'warn'
    },
    EM_PREPARO: {
      texto: 'Em preparo',
      severity: 'info'
    },
    CONCLUIDO: {
      texto: 'Concluído',
      severity: 'success'
    },
    CANCELADO: {
      texto: 'Cancelado',
      severity: 'danger'
    }
  }

  return configuracoes[status] || {
    texto: status,
    severity: 'secondary'
  }
}
</script>

<template>
  <main class="container">
    <AppHeader />

    <CartPanel />

    <section class="historico-pedidos">
      <div class="cabecalho-listagem">
        <div>
          <h2>
            Pedidos da comanda
            {{ comandaAtual?.numero }}
          </h2>

          <p class="texto-secundario">
            Acompanhe aqui o preparo dos seus pedidos.
          </p>
        </div>

        <Button
            label="Atualizar"
            icon="pi pi-refresh"
            severity="secondary"
            :loading="carregandoPedidosComanda"
            @click="carregarPedidosComanda()"
        />
      </div>

      <div
          v-if="carregandoPedidosComanda"
          class="pagina-centralizada"
      >
        <ProgressSpinner />
      </div>

      <p
          v-else-if="pedidosComanda.length === 0"
          class="texto-secundario"
      >
        Nenhum pedido foi enviado nesta comanda.
      </p>

      <div
          v-else
          class="grade-pedidos"
      >
        <Card
            v-for="pedido in pedidosComanda"
            :key="pedido.id"
            class="pedido-cliente-card"
        >
          <template #title>
            Pedido #{{ pedido.id }}
          </template>

          <template #subtitle>
            {{ formatarData(pedido.dataPedido) }}
          </template>

          <template #content>
            <div class="detalhes-pedido">
              <ul class="lista-itens-pedido">
                <li
                    v-for="item in pedido.itens"
                    :key="item.id"
                >
                  <strong>
                    {{ item.quantidade }}x
                    {{ item.produtoNome }}
                  </strong>

                  — {{ formatarPreco(item.subtotal) }}
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
            <div class="status-pedido">
              <Tag
                  :value="
                    dadosStatus(pedido.status).texto
                  "
                  :severity="
                    dadosStatus(pedido.status).severity
                  "
              />
            </div>
          </template>
        </Card>
      </div>
    </section>
  </main>
</template>