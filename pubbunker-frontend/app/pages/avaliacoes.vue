<script setup>
definePageMeta({
  role: 'CLIENTE'
})

const {
  podeAvaliar,
  avaliacoes,
  carregandoAvaliacoes,
  carregarAvaliacoes,
  criarAvaliacao,
  atualizarAvaliacao,
  excluirAvaliacao
} = useAvaliacoes()

const { abrirPopup } = usePopup()
const { codigoComanda } = useComanda()

const salvando = ref(false)
const avaliacaoEditandoId = ref(null)
const avaliacaoParaExcluir = ref(null)
const confirmacaoAvaliacaoVisivel = ref(false)
const excluindoAvaliacao = ref(false)

const mensagemErro = (erro, alternativa) =>
  Object.values(erro.response?.data?.campos || {}).join(' ') ||
  erro.response?.data?.mensagem || alternativa

const formularioVazio = () => ({
  nota: 5,
  nomeCliente: '',
  comentario: ''
})

const form = reactive(formularioVazio())

onMounted(carregarAvaliacoes)

const limparFormulario = () => {
  Object.assign(form, formularioVazio())
  avaliacaoEditandoId.value = null
}

const salvar = async () => {
  if (salvando.value || excluindoAvaliacao.value) return

  if (!podeAvaliar.value) {
    abrirPopup(
      'Comanda necessária',
      'Acesse uma comanda aberta para avaliar.',
      'erro'
    )
    return
  }

  if (
    !Number.isInteger(Number(form.nota)) ||
    Number(form.nota) < 1 ||
    Number(form.nota) > 5
  ) {
    abrirPopup('Campo obrigatório', 'Informe uma nota de 1 a 5.', 'erro')
    return
  }

  if (form.nomeCliente.length > 100 || form.comentario.length > 255) {
    abrirPopup('Confira sua avaliação', 'Use até 100 caracteres no nome e até 255 no comentário.', 'erro')
    return
  }

  salvando.value = true

  try {
    if (avaliacaoEditandoId.value) {
      const salvo = await atualizarAvaliacao(
        avaliacaoEditandoId.value,
        form
      )
      if (!salvo) return

      abrirPopup(
        'Avaliação atualizada',
        'Sua avaliação foi atualizada com sucesso.'
      )
    } else {
      const salvo = await criarAvaliacao(form)
      if (!salvo) return

      abrirPopup(
        'Avaliação enviada',
        'Sua avaliação foi registrada com sucesso.'
      )
    }

    limparFormulario()
  } catch (erro) {
    abrirPopup(
      'Não foi possível salvar sua avaliação',
      mensagemErro(erro, 'Não foi possível salvar a avaliação.'),
      'erro',
      erro.response?.status === 429 ? 60 : null
    )
  } finally {
    salvando.value = false
  }
}

const editar = avaliacao => {
  if (salvando.value || excluindoAvaliacao.value) return

  avaliacaoEditandoId.value = avaliacao.id
  form.nota = avaliacao.nota
  form.nomeCliente = avaliacao.nomeCliente || ''
  form.comentario = avaliacao.comentario || ''

  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const solicitarExclusao = id => {
  if (excluindoAvaliacao.value || salvando.value) return

  const avaliacao = avaliacoes.value.find(item => item.id === id)
  if (!avaliacao) return

  avaliacaoParaExcluir.value = avaliacao
  confirmacaoAvaliacaoVisivel.value = true
}

const excluir = async () => {
  if (
    !avaliacaoParaExcluir.value ||
    excluindoAvaliacao.value ||
    salvando.value
  ) return

  const id = avaliacaoParaExcluir.value.id
  excluindoAvaliacao.value = true

  try {
    const excluido = await excluirAvaliacao(id)
    if (!excluido) return

    if (avaliacaoEditandoId.value === id) limparFormulario()

    confirmacaoAvaliacaoVisivel.value = false

    abrirPopup(
      'Avaliação excluída',
      'Sua avaliação foi removida com sucesso.'
    )
  } catch (erro) {
    abrirPopup(
      'Não foi possível excluir sua avaliação',
      mensagemErro(erro, 'Não foi possível excluir a avaliação.'),
      'erro',
      erro.response?.status === 429 ? 60 : null
    )
  } finally {
    excluindoAvaliacao.value = false
  }
}

watch(codigoComanda, () => {
  limparFormulario()
  confirmacaoAvaliacaoVisivel.value = false
  avaliacaoParaExcluir.value = null
  carregarAvaliacoes()
})

const formatarData = valor =>
  new Date(valor).toLocaleString('pt-BR')
</script>

<template>
  <main class="container">
    <AppHeader />

    <p v-if="!podeAvaliar" class="painel texto-secundario">
      Acesse uma comanda aberta para enviar sua avaliação.
      O nome é opcional e você pode avaliar sem criar uma conta.
    </p>

    <Card v-if="podeAvaliar" class="painel">
      <template #title>
        {{ avaliacaoEditandoId ? 'Editar avaliação' : 'Nova avaliação' }}
      </template>

      <template #content>
        <div class="form-grid">
          <div class="campo campo-largo">
            <label for="nome-cliente">Nome (opcional)</label>
            <InputText
              id="nome-cliente"
              v-model="form.nomeCliente"
              maxlength="100"
              :disabled="salvando || excluindoAvaliacao"
              placeholder="Como você quer aparecer?"
              fluid
            />
            <small>
              Se deixar vazio, sua avaliação aparecerá como Anônimo.
            </small>
          </div>

          <div class="campo campo-largo">
            <label>Sua nota</label>
            <Rating
              v-model="form.nota"
              :disabled="salvando || excluindoAvaliacao"
              :cancel="false"
            />
          </div>

          <div class="campo campo-largo">
            <label for="comentario">Comentário (opcional)</label>
            <Textarea
              id="comentario"
              v-model="form.comentario"
              :disabled="salvando || excluindoAvaliacao"
              rows="4"
              maxlength="255"
              auto-resize
              fluid
            />
            <small>{{ form.comentario.length }}/255</small>
          </div>
        </div>
      </template>

      <template #footer>
        <div class="acoes-card">
          <Button
            :label="avaliacaoEditandoId
              ? 'Atualizar avaliação'
              : 'Enviar avaliação'"
            icon="pi pi-save"
            :loading="salvando"
            :disabled="salvando || excluindoAvaliacao"
            @click="salvar"
          />

          <Button
            v-if="avaliacaoEditandoId"
            label="Cancelar edição"
            severity="secondary"
            :disabled="salvando || excluindoAvaliacao"
            @click="limparFormulario"
          />
        </div>
      </template>
    </Card>

    <section v-if="podeAvaliar">
      <h2>Avaliações deste atendimento</h2>

      <p v-if="carregandoAvaliacoes" class="texto-secundario">
        Carregando avaliações...
      </p>
      <p v-else-if="avaliacoes.length === 0" class="texto-secundario">
        Ainda não há avaliações neste atendimento.
      </p>

      <div v-else class="grade-avaliacoes setor-rolavel" role="region" aria-label="Avaliações da comanda" tabindex="0">
        <Card v-for="avaliacao in avaliacoes" :key="avaliacao.id">
          <template #title>
            Avaliação #{{ avaliacao.id }}
          </template>

          <template #subtitle>
            {{ avaliacao.nomeCliente || avaliacao.usuarioNome || 'Anônimo' }}
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
          </template>

          <template #footer>
            <div class="acoes-card">
              <Button
                label="Editar"
                icon="pi pi-pencil"
                severity="warn"
                :disabled="salvando || excluindoAvaliacao"
                @click="editar(avaliacao)"
              />
              <Button
                label="Excluir"
                icon="pi pi-trash"
                severity="danger"
                :disabled="salvando || excluindoAvaliacao"
                @click="solicitarExclusao(avaliacao.id)"
              />
            </div>
          </template>
        </Card>
      </div>
    </section>

    <Dialog
      v-model:visible="confirmacaoAvaliacaoVisivel"
      modal
      header="Excluir avaliação"
      :closable="false"
      :draggable="false"
      :close-on-escape="!excluindoAvaliacao"
      :style="{ width: 'min(92vw, 420px)' }"
      @after-hide="avaliacaoParaExcluir = null"
    >
      <p v-if="avaliacaoParaExcluir">
        Tem certeza que deseja excluir sua avaliação
        <strong>#{{ avaliacaoParaExcluir.id }}</strong>?
      </p>

      <template #footer>
        <Button
          label="Cancelar"
          severity="secondary"
          text
          :disabled="excluindoAvaliacao"
          @click="confirmacaoAvaliacaoVisivel = false"
        />
        <Button
          label="Excluir"
          severity="danger"
          :loading="excluindoAvaliacao"
          :disabled="excluindoAvaliacao"
          @click="excluir"
        />
      </template>
    </Dialog>
  </main>
</template>