<script setup>
definePageMeta({ role: 'ADMIN' })

const { $api } = useNuxtApp()
const { usuarioId, role } = useAuth()

const avaliacoes = ref([])
const carregando = ref(false)
const erro = ref('')
let versaoConsulta = 0

const podeConsultar = () =>
  role.value === 'ADMIN' && Boolean(usuarioId.value)

const carregar = async () => {
  const versao = ++versaoConsulta

  if (!podeConsultar()) {
    avaliacoes.value = []
    carregando.value = false
    erro.value = 'Entre com uma conta de administrador para consultar.'
    return
  }

  const idSolicitado = usuarioId.value
  carregando.value = true
  erro.value = ''

  try {
    const { data } = await $api.get('/avaliacoes')

    if (
      versao !== versaoConsulta ||
      usuarioId.value !== idSolicitado ||
      !podeConsultar()
    ) return

    avaliacoes.value = data
  } catch (falha) {
    if (
      versao !== versaoConsulta ||
      usuarioId.value !== idSolicitado ||
      !podeConsultar()
    ) return

    avaliacoes.value = []
    erro.value = falha.response?.data?.mensagem ||
      'Não foi possível carregar as avaliações.'
  } finally {
    if (versao === versaoConsulta) carregando.value = false
  }
}

watch([usuarioId, role], () => {
  versaoConsulta++
  avaliacoes.value = []
  erro.value = ''
  carregando.value = false

  if (podeConsultar()) carregar()
})

onBeforeUnmount(() => {
  versaoConsulta++
  avaliacoes.value = []
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
        <h2>Avaliações recebidas</h2>
        <p class="texto-secundario">
          Total: {{ avaliacoes.length }}
        </p>
      </div>

      <Button
        label="Atualizar"
        icon="pi pi-refresh"
        :loading="carregando"
        :disabled="carregando"
        @click="carregar"
      />
    </div>

    <p v-if="carregando" class="texto-secundario">
      Carregando avaliações...
    </p>

    <p v-else-if="erro" class="painel" role="alert">
      {{ erro }}
    </p>

    <p v-else-if="avaliacoes.length === 0" class="texto-secundario">
      Ainda não há avaliações recebidas.
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
      </Card>
    </div>
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
</style>