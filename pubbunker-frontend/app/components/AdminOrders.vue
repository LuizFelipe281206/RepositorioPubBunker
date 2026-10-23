<script setup>
const {
  pedidos,
  carregandoPedidos,
  processandoPedidos,
  pedidoProcessandoId,
  erroPedidos,
  carregarPedidos,
  atualizarStatus,
  editarPedido,
  fecharPedido
} = usePedidos()

const statusSelecionado = ref('PENDENTE')
const pedidoParaArquivar = ref(null)
const confirmacaoPedidoVisivel = ref(false)
const consultaEmAndamento = ref(false)
const pedidoEmEdicao = ref(null)
const edicaoVisivel = computed({
  get: () => Boolean(pedidoEmEdicao.value),
  set: visivel => { if (!visivel && !processandoPedidos.value) pedidoEmEdicao.value = null }
})
const modalAberto = computed(() => confirmacaoPedidoVisivel.value || edicaoVisivel.value)

const opcoesStatus = [
  {
    valor: 'PENDENTE',
    titulo: 'Pendentes',
    etiqueta: 'Pendente',
    severity: 'warn',
    vazio: 'Nenhum pedido pendente.'
  },
  {
    valor: 'EM_PREPARO',
    titulo: 'Em preparo',
    etiqueta: 'Em preparo',
    severity: 'info',
    vazio: 'Nenhum pedido em preparo.'
  },
  {
    valor: 'CONCLUIDO',
    titulo: 'Concluídos',
    etiqueta: 'Concluído',
    severity: 'success',
    vazio: 'Nenhum pedido concluído.'
  }
]

const opcaoAtual = computed(() =>
  opcoesStatus.find(opcao => opcao.valor === statusSelecionado.value) ||
  opcoesStatus[0]
)

const contagens = computed(() =>
  Object.fromEntries(opcoesStatus.map(opcao => [
    opcao.valor,
    pedidos.value.filter(pedido => pedido.status === opcao.valor).length
  ]))
)

const pedidosFiltrados = computed(() =>
  pedidos.value.filter(pedido => pedido.status === opcaoAtual.value.valor)
)

const ocupado = computed(() =>
  consultaEmAndamento.value || processandoPedidos.value
)

const selecionarStatus = status => {
  if (
    processandoPedidos.value || modalAberto.value ||
    !opcoesStatus.some(opcao => opcao.valor === status)
  ) return

  statusSelecionado.value = status
}

const atualizarPedidos = async (silencioso = false) => {
  if (ocupado.value || modalAberto.value) return
  consultaEmAndamento.value = true

  try {
    await carregarPedidos(silencioso)
  } finally {
    consultaEmAndamento.value = false
  }
}

const mudarStatus = async (pedido, status) => {
  if (ocupado.value || modalAberto.value) return
  await atualizarStatus(pedido.id, status)
}

const abrirEdicao = pedido => {
  if (ocupado.value || modalAberto.value || !['PENDENTE', 'EM_PREPARO'].includes(pedido.status)) return
  pedidoEmEdicao.value = { id: pedido.id }
}
const salvarEdicao = async dados => {
  if (ocupado.value || !pedidoEmEdicao.value) return
  if (await editarPedido(pedidoEmEdicao.value.id, dados)) pedidoEmEdicao.value = null
}

const fecharConfirmacao = () => {
  if (processandoPedidos.value) return
  confirmacaoPedidoVisivel.value = false
  pedidoParaArquivar.value = null
}

const solicitarArquivamento = pedido => {
  if (ocupado.value || modalAberto.value) return

  const atual = pedidos.value.find(item => item.id === pedido.id)
  if (!atual || atual.status !== 'CONCLUIDO') return

  pedidoParaArquivar.value = { id: atual.id }
  confirmacaoPedidoVisivel.value = true
}

const confirmarArquivamento = async () => {
  if (ocupado.value || !pedidoParaArquivar.value) return

  const atual = pedidos.value.find(
    item => item.id === pedidoParaArquivar.value.id
  )

  if (!atual || atual.status !== 'CONCLUIDO') {
    fecharConfirmacao()
    return
  }

  if (await fecharPedido(atual)) fecharConfirmacao()
}

watch(pedidos, () => {
  if (pedidoEmEdicao.value && !pedidos.value.some(item => item.id === pedidoEmEdicao.value.id)) {
    pedidoEmEdicao.value = null
  }
  if (
    pedidoParaArquivar.value &&
    !pedidos.value.some(item =>
      item.id === pedidoParaArquivar.value.id &&
      item.status === 'CONCLUIDO'
    )
  ) {
    confirmacaoPedidoVisivel.value = false
    pedidoParaArquivar.value = null
  }
}, { flush: 'sync' })

let intervaloAtualizacao = null

onMounted(() => {
  atualizarPedidos()
  intervaloAtualizacao = setInterval(() => atualizarPedidos(true), 5000)
})

onBeforeUnmount(() => {
  clearInterval(intervaloAtualizacao)
  confirmacaoPedidoVisivel.value = false
  pedidoParaArquivar.value = null
  pedidoEmEdicao.value = null
})

const formatarPreco = valor =>
  Number(valor).toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  })

const formatarData = valor =>
  valor ? new Date(valor).toLocaleString('pt-BR') : ''
</script>

<template>
  <section>
    <div class="cabecalho-pedidos">
      <div>
        <h2>Pedidos recebidos</h2>
        <p class="texto-secundario">
          Total no painel: {{ pedidos.length }}.
        </p>
      </div>

      <Button
        label="Atualizar"
        icon="pi pi-refresh"
        severity="secondary"
        :loading="carregandoPedidos"
        :disabled="ocupado || modalAberto"
        @click="atualizarPedidos()"
      />
    </div>

    <div class="filtros-pedidos" role="group" aria-label="Filtrar pedidos por status">
      <button
        v-for="opcao in opcoesStatus"
        :key="opcao.valor"
        type="button"
        class="filtro-pedido"
        :class="{ 'filtro-pedido-ativo': statusSelecionado === opcao.valor }"
        :aria-pressed="statusSelecionado === opcao.valor"
        :disabled="processandoPedidos || modalAberto"
        @click="selecionarStatus(opcao.valor)"
      >
        <span>{{ opcao.titulo }}</span>
        <span class="contador-status">{{ contagens[opcao.valor] }}</span>
      </button>
    </div>

    <p v-if="erroPedidos" class="erro-pedidos" role="alert">
      {{ erroPedidos }}
    </p>

    <p v-if="carregandoPedidos && !pedidos.length" class="texto-secundario">
      Carregando pedidos...
    </p>
    <p v-else-if="!pedidosFiltrados.length && !erroPedidos" class="texto-secundario">
      {{ opcaoAtual.vazio }}
    </p>

    <div v-else class="grade-pedidos setor-rolavel" role="region" aria-label="Pedidos por status" tabindex="0" :aria-busy="carregandoPedidos">
      <Card v-for="pedido in pedidosFiltrados" :key="pedido.id" class="pedido-card">
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
          Pedido realizado pela comanda
          <br>
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
                    {{ formatarPreco(item.precoUnitario) }}
                    cada — subtotal:
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

                    <span v-if="adicional.preco != null">
                      {{ formatarPreco(adicional.preco) }}
                      por unidade
                    </span>
                  </li>
                </ul>
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
            <Tag :value="opcaoAtual.etiqueta" :severity="opcaoAtual.severity" />

            <Button v-if="['PENDENTE', 'EM_PREPARO'].includes(pedido.status)"
              label="Editar pedido" icon="pi pi-pencil" severity="secondary" outlined
              :disabled="ocupado || modalAberto" @click="abrirEdicao(pedido)" />

            <Button
              v-if="pedido.status === 'PENDENTE'"
              label="Em preparo"
              severity="warn"
              :loading="processandoPedidos && pedidoProcessandoId === pedido.id"
              :disabled="ocupado || modalAberto"
              @click="mudarStatus(pedido, 'EM_PREPARO')"
            />

            <Button
              v-else-if="pedido.status === 'EM_PREPARO'"
              label="Concluir"
              severity="success"
              :loading="processandoPedidos && pedidoProcessandoId === pedido.id"
              :disabled="ocupado || modalAberto"
              @click="mudarStatus(pedido, 'CONCLUIDO')"
            />

            <Button
              v-else-if="pedido.status === 'CONCLUIDO'"
              label="Arquivar"
              severity="secondary"
              :disabled="ocupado || modalAberto"
              @click="solicitarArquivamento(pedido)"
            />
          </div>
        </template>
      </Card>
    </div>

    <Dialog v-model:visible="edicaoVisivel" modal :header="`Editar pedido #${pedidoEmEdicao?.id || ''}`"
      :closable="false" :draggable="false" :close-on-escape="!processandoPedidos" :style="{ width: 'min(94vw, 740px)' }">
      <OrderEditForm v-if="pedidoEmEdicao" :key="pedidoEmEdicao.id" :pedido-id="pedidoEmEdicao.id"
        :salvando="processandoPedidos" @salvar="salvarEdicao" @cancelar="edicaoVisivel = false" />
    </Dialog>

    <Dialog
      v-model:visible="confirmacaoPedidoVisivel"
      modal
      header="Arquivar pedido"
      :closable="false"
      :draggable="false"
      :close-on-escape="!processandoPedidos"
      :style="{ width: 'min(92vw, 420px)' }"
      @hide="fecharConfirmacao"
    >
      <p v-if="pedidoParaArquivar">
        Tem certeza que deseja arquivar o pedido
        <strong>#{{ pedidoParaArquivar.id }}</strong>?
      </p>

      <p class="texto-secundario">
        Ele será removido da tela de pedidos recebidos.
      </p>

      <template #footer>
        <Button
          label="Cancelar"
          severity="secondary"
          text
          :disabled="processandoPedidos"
          @click="fecharConfirmacao"
        />
        <Button
          label="Arquivar"
          severity="danger"
          :loading="processandoPedidos"
          :disabled="processandoPedidos"
          @click="confirmarArquivamento"
        />
      </template>
    </Dialog>
  </section>
</template>

<style scoped>
.cabecalho-pedidos {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 18px;
}
.cabecalho-pedidos h2 { margin-bottom: 6px; }
.cabecalho-pedidos p { margin: 0; }
.filtros-pedidos {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}
.filtro-pedido {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-width: 0;
  min-height: 52px;
  padding: 12px 16px;
  border: 1px solid var(--bunker-border);
  border-radius: 8px;
  background: var(--bunker-card);
  color: var(--bunker-text);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}
.filtro-pedido:not(:disabled):hover {
  border-color: var(--bunker-wine);
}
.filtro-pedido-ativo {
  border-color: var(--bunker-wine);
  background: var(--bunker-wine);
  color: white;
}
.filtro-pedido:focus-visible {
  outline: 3px solid var(--bunker-amber);
  outline-offset: 2px;
}
.filtro-pedido:disabled { cursor: wait; opacity: 0.7; }
.contador-status {
  display: grid;
  place-items: center;
  min-width: 28px;
  min-height: 28px;
  padding: 0 6px;
  border-radius: 20px;
  background: var(--bunker-cream);
  color: var(--bunker-dark);
}
.filtro-pedido-ativo .contador-status {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}
.grade-pedidos {
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 280px), 1fr));
}
.pedido-card { min-width: 0; }
.acoes-card { align-items: center; justify-content: space-between; }
.erro-pedidos { color: var(--bunker-wine); }
@media (max-width: 600px) {
  .filtros-pedidos { grid-template-columns: 1fr; }
}
</style>

