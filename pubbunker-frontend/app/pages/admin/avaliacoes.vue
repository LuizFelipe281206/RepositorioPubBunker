<script setup>
definePageMeta({ role: 'ADMIN' })

const { $api } = useNuxtApp()
const { usuarioId, role } = useAuth()
const { abrirPopup } = usePopup()

const avaliacoes = ref([])
const carregando = ref(false)
const erro = ref('')
const mostrarArquivadas = ref(false)
const confirmacaoVisivel = ref(false)
const selecionada = ref(null)
const acaoSelecionada = ref(null)
const processando = ref(false)

let paginaAtiva = true
let versaoConsulta = 0
let versaoAcao = 0

const podeConsultar = () =>
  paginaAtiva && role.value === 'ADMIN' && Boolean(usuarioId.value)

const carregar = async () => {
  if (processando.value) return
  const versao = ++versaoConsulta

  if (!podeConsultar()) {
    avaliacoes.value = []
    carregando.value = false
    erro.value = 'Entre com uma conta de administrador para consultar.'
    return
  }

  const idSolicitado = usuarioId.value
  const arquivoSolicitado = mostrarArquivadas.value
  carregando.value = true
  erro.value = ''

  const respostaAtual = () =>
    versao === versaoConsulta &&
    usuarioId.value === idSolicitado &&
    mostrarArquivadas.value === arquivoSolicitado &&
    podeConsultar()

  try {
    const { data } = await $api.get(
      arquivoSolicitado ? '/avaliacoes/arquivadas' : '/avaliacoes'
    )
    if (respostaAtual()) avaliacoes.value = data
  } catch (falha) {
    if (!respostaAtual()) return
    avaliacoes.value = []
    erro.value = falha.response?.data?.mensagem ||
      'Não foi possível carregar as avaliações.'
  } finally {
    if (versao === versaoConsulta) carregando.value = false
  }
}

const limparConfirmacao = () => {
  confirmacaoVisivel.value = false
  selecionada.value = null
  acaoSelecionada.value = null
}

const fecharConfirmacao = () => {
  if (!processando.value) limparConfirmacao()
}

const trocarLista = () => {
  if (!podeConsultar() || carregando.value || processando.value) return

  limparConfirmacao()
  mostrarArquivadas.value = !mostrarArquivadas.value
  avaliacoes.value = []

  return carregar()
}

const solicitarAcao = avaliacao => {
  if (
    !podeConsultar() ||
    carregando.value ||
    processando.value ||
    confirmacaoVisivel.value ||
    !avaliacoes.value.some(item => item.id === avaliacao?.id)
  ) return

  selecionada.value = avaliacao
  acaoSelecionada.value = mostrarArquivadas.value ? 'excluir' : 'arquivar'
  confirmacaoVisivel.value = true
}

const confirmarAcao = async () => {
  const acaoEsperada = mostrarArquivadas.value ? 'excluir' : 'arquivar'

  if (
    !podeConsultar() ||
    carregando.value ||
    processando.value ||
    !confirmacaoVisivel.value ||
    !selecionada.value ||
    acaoSelecionada.value !== acaoEsperada ||
    !avaliacoes.value.some(item => item.id === selecionada.value.id)
  ) return

  const id = selecionada.value.id
  const idSolicitado = usuarioId.value
  const arquivoSolicitado = mostrarArquivadas.value
  const exclusao = acaoSelecionada.value === 'excluir'
  const versao = ++versaoAcao

  versaoConsulta++
  processando.value = true

  const respostaAtual = () =>
    versao === versaoAcao &&
    usuarioId.value === idSolicitado &&
    mostrarArquivadas.value === arquivoSolicitado &&
    podeConsultar()

  try {
    if (exclusao) {
      await $api.delete(
        `/avaliacoes/arquivadas/${encodeURIComponent(id)}`
      )
    } else {
      await $api.patch(
        `/avaliacoes/${encodeURIComponent(id)}/arquivar`
      )
    }

    if (!respostaAtual()) return

    avaliacoes.value = avaliacoes.value.filter(item => item.id !== id)
    limparConfirmacao()

    abrirPopup(
      exclusao ? 'Avaliação excluída' : 'Avaliação arquivada',
      exclusao
        ? 'A avaliação foi removida das listas do sistema.'
        : 'Você pode consultá-la na lista de arquivadas.'
    )
  } catch (falha) {
    if (!respostaAtual()) return

    abrirPopup(
      'Erro',
      falha.response?.data?.mensagem ||
        (exclusao
          ? 'Não foi possível excluir a avaliação.'
          : 'Não foi possível arquivar a avaliação.'),
      'erro',
      falha.response?.status === 429 ? 60 : null
    )
  } finally {
    if (versao === versaoAcao) processando.value = false
  }
}

watch([usuarioId, role], () => {
  versaoConsulta++
  versaoAcao++

  avaliacoes.value = []
  erro.value = ''
  carregando.value = false
  processando.value = false
  limparConfirmacao()

  if (podeConsultar()) carregar()
})

onBeforeUnmount(() => {
  paginaAtiva = false
  versaoConsulta++
  versaoAcao++

  avaliacoes.value = []
  limparConfirmacao()
})

onMounted(carregar)

const formatarData = valor =>
  valor ? new Date(valor).toLocaleString('pt-BR') : '—'
</script>

<template>
  <main class="container">
    <AppHeader />

    <div class="cabecalho-avaliacoes">
      <div>
        <h2>
          {{ mostrarArquivadas ? 'Avaliações arquivadas' : 'Avaliações recebidas' }}
        </h2>

        <p class="texto-secundario">
          Total: {{ avaliacoes.length }}
        </p>
      </div>

      <div class="acoes-avaliacoes">
        <Button
          :label="mostrarArquivadas ? 'Ver ativas' : 'Ver arquivadas'"
          icon="pi pi-inbox"
          severity="secondary"
          :disabled="carregando || processando"
          @click="trocarLista"
        />

        <Button
          label="Atualizar"
          icon="pi pi-refresh"
          :loading="carregando"
          :disabled="carregando || processando"
          @click="carregar"
        />
      </div>
    </div>

    <p v-if="carregando" class="texto-secundario">
      Carregando avaliações...
    </p>

    <p v-else-if="erro" class="painel" role="alert">
      {{ erro }}
    </p>

    <p v-else-if="avaliacoes.length === 0" class="texto-secundario">
      {{
        mostrarArquivadas
          ? 'Nenhuma avaliação arquivada.'
          : 'Nenhuma avaliação ativa.'
      }}
    </p>

    <div v-else class="grade-avaliacoes">
      <Card v-for="avaliacao in avaliacoes" :key="avaliacao.id">
        <template #title>
          {{ avaliacao.nomeCliente || avaliacao.usuarioNome || 'Anônimo' }}
        </template>

        <template #subtitle>
          {{
            avaliacao.comandaNumero != null
              ? 'Comanda ' + avaliacao.comandaNumero
              : 'Avaliação de conta'
          }}
          · {{ formatarData(avaliacao.dataHora) }}
        </template>

        <template #content>
          <Rating
            :model-value="avaliacao.nota"
            :cancel="false"
            readonly
          />

          <p class="comentario-avaliacao">
            {{ avaliacao.comentario || 'Sem comentário.' }}
          </p>

          <small
            v-if="avaliacao.dataAberturaAtendimento"
            class="texto-secundario"
          >
            Abertura do atendimento:
            {{ formatarData(avaliacao.dataAberturaAtendimento) }}
          </small>
        </template>

        <template #footer>
          <Button
            :label="mostrarArquivadas ? 'Excluir' : 'Arquivar'"
            :icon="mostrarArquivadas ? 'pi pi-trash' : 'pi pi-inbox'"
            :severity="mostrarArquivadas ? 'danger' : 'secondary'"
            :disabled="carregando || processando"
            @click="solicitarAcao(avaliacao)"
          />
        </template>
      </Card>
    </div>

    <Dialog
      v-model:visible="confirmacaoVisivel"
      :header="acaoSelecionada === 'excluir' ? 'Excluir avaliação' : 'Arquivar avaliação'"
      modal
      :closable="!processando"
      :close-on-escape="!processando"
      :style="{ width: 'min(420px, 92vw)' }"
      @hide="fecharConfirmacao"
    >
      <p v-if="acaoSelecionada === 'excluir'">
        Esta avaliação será removida das listas do sistema, inclusive da
        comanda. Não será possível restaurá-la pelo painel.
      </p>

      <p v-else>
        Esta avaliação sairá da lista de ativas e ficará disponível em
        Arquivadas.
      </p>

      <template #footer>
        <Button
          label="Cancelar"
          severity="secondary"
          :disabled="processando"
          @click="fecharConfirmacao"
        />

        <Button
          :label="acaoSelecionada === 'excluir' ? 'Excluir' : 'Arquivar'"
          :icon="acaoSelecionada === 'excluir' ? 'pi pi-trash' : 'pi pi-inbox'"
          :severity="acaoSelecionada === 'excluir' ? 'danger' : 'secondary'"
          :loading="processando"
          :disabled="processando"
          @click="confirmarAcao"
        />
      </template>
    </Dialog>
  </main>
</template>

<style scoped>
.cabecalho-avaliacoes {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin: 24px 0;
}

.acoes-avaliacoes {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>