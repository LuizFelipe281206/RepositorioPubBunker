<script setup>
definePageMeta({
  role: 'CLIENTE'
})

const {
  comandaAtual,
  comandaAtiva
} = useComanda()

const {
  pedidosComanda,
  carregandoPedidosComanda,
  carregarPedidosComanda
} = usePedidosComanda()

const {
  pedidosCliente,
  carregandoPedidosCliente,
  carregarPedidosCliente
} = usePedidosCliente()

let intervaloAtualizacao = null

const pedidosExibidos = computed(() => {
  return comandaAtiva.value
      ? pedidosComanda.value
      : pedidosCliente.value
})

const carregandoPedidos = computed(() => {
  return comandaAtiva.value
      ? carregandoPedidosComanda.value
      : carregandoPedidosCliente.value
})

const tituloPedidos = computed(() => {
  if (comandaAtiva.value) {
    return `Pedidos da comanda ${
        comandaAtual.value?.numero || ''
    }`
  }

  return 'Meus pedidos'
})

const carregarPedidos = async (
    silencioso = false
) => {
  if (comandaAtiva.value) {
    await carregarPedidosComanda(silencioso)
    return
  }

  await carregarPedidosCliente(silencioso)
}

onMounted(async () => {
  await carregarPedidos()

  intervaloAtualizacao = setInterval(
      () => carregarPedidos(true),
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
  if (!valor) {
    return ''
  }

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
          <h2>{{ tituloPedidos }}</h2>

          <p class="texto-secundario">
            Acompanhe aqui o preparo dos seus pedidos.
          </p>
        </div>

        <Button
            label="Atualizar"
            icon="pi pi-refresh"
            severity="secondary"
            :loading="carregandoPedidos"
            @click="carregarPedidos()"
        />
      </div>

      <div
          v-if="carregandoPedidos"
          class="pagina-centralizada"
      >
        <ProgressSpinner />
      </div>

      <p
          v-else-if="pedidosExibidos.length === 0"
          class="texto-secundario"
      >
        Nenhum pedido foi encontrado.
      </p>

      <div
          v-else
          class="grade-pedidos"
      >
        <Card
            v-for="pedido in pedidosExibidos"
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
              <ul
                  v-if="pedido.itens?.length"
                  class="lista-itens-pedido"
              >
                <li
    v-for="item in pedido.itens"
    :key="item.id"
>
  <div class="item-pedido-principal">
    <strong>
      {{ item.quantidade }}x
      {{ item.produtoNome }}
    </strong>

    <span>
      {{ formatarPreco(item.subtotal) }}
    </span>
  </div>

  <ul
      v-if="item.adicionais?.length"
      class="lista-adicionais-pedido"
  >
    <li
        v-for="adicional in item.adicionais"
        :key="adicional.id"
    >
      + {{ adicional.nome }}

      <span>
        {{ formatarPreco(adicional.preco) }}
        por unidade
      </span>
    </li>
  </ul>
</li>
              </ul>

              <p
                  v-else
                  class="texto-secundario"
              >
                Nenhum item encontrado neste pedido.
              </p>

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