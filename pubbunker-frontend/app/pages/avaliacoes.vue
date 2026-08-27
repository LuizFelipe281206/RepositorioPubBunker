<script setup>
definePageMeta({
  role: 'CLIENTE'
})

const {
  avaliacoes,
  carregandoAvaliacoes,
  carregarAvaliacoes,
  criarAvaliacao,
  atualizarAvaliacao,
  excluirAvaliacao
} = useAvaliacoes()

const { abrirPopup } = usePopup()

const avaliacaoEditandoId = ref(null)

const formularioVazio = () => ({
  nota: 5,
  comentario: ''
})

const form = reactive(formularioVazio())

onMounted(carregarAvaliacoes)

const limparFormulario = () => {
  Object.assign(
      form,
      formularioVazio()
  )

  avaliacaoEditandoId.value = null
}

const salvar = async () => {
  if (
      !form.nota ||
      !form.comentario.trim()
  ) {
    abrirPopup(
        'Campos obrigatórios',
        'Informe uma nota e um comentário.',
        'erro'
    )

    return
  }

  try {
    if (avaliacaoEditandoId.value) {
      await atualizarAvaliacao(
          avaliacaoEditandoId.value,
          form
      )

      abrirPopup(
          'Avaliação atualizada',
          'Sua avaliação foi atualizada com sucesso.'
      )
    } else {
      await criarAvaliacao(form)

      abrirPopup(
          'Avaliação enviada',
          'Sua avaliação foi registrada com sucesso.'
      )
    }

    limparFormulario()
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível salvar a avaliação.',
        'erro'
    )
  }
}

const editar = (avaliacao) => {
  avaliacaoEditandoId.value = avaliacao.id

  form.nota = avaliacao.nota
  form.comentario = avaliacao.comentario

  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const excluir = async (id) => {
  try {
    await excluirAvaliacao(id)

    if (avaliacaoEditandoId.value === id) {
      limparFormulario()
    }

    abrirPopup(
        'Avaliação excluída',
        'Sua avaliação foi removida com sucesso.'
    )
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível excluir a avaliação.',
        'erro'
    )
  }
}

const formatarData = valor =>
    new Date(valor).toLocaleString('pt-BR')
</script>

<template>
  <main class="container">
    <AppHeader />

    <Card class="painel">
      <template #title>
        {{
          avaliacaoEditandoId
              ? 'Editar avaliação'
              : 'Nova avaliação'
        }}
      </template>

      <template #content>
        <div class="form-grid">
          <div class="campo campo-largo">
            <label>Sua nota</label>

            <Rating
                v-model="form.nota"
                :cancel="false"
            />
          </div>

          <div class="campo campo-largo">
            <label for="comentario">
              Comentário
            </label>

            <Textarea
                id="comentario"
                v-model="form.comentario"
                rows="4"
                maxlength="255"
                auto-resize
                fluid
            />

            <small>
              {{ form.comentario.length }}/255
            </small>
          </div>
        </div>
      </template>

      <template #footer>
        <div class="acoes-card">
          <Button
              :label="
                avaliacaoEditandoId
                    ? 'Atualizar avaliação'
                    : 'Enviar avaliação'
              "
              icon="pi pi-save"
              @click="salvar"
          />

          <Button
              v-if="avaliacaoEditandoId"
              label="Cancelar edição"
              severity="secondary"
              @click="limparFormulario"
          />
        </div>
      </template>
    </Card>

    <section>
      <h2>Minhas avaliações</h2>

      <p
          v-if="carregandoAvaliacoes"
          class="texto-secundario"
      >
        Carregando avaliações...
      </p>

      <p
          v-else-if="avaliacoes.length === 0"
          class="texto-secundario"
      >
        Você ainda não cadastrou avaliações.
      </p>

      <div
          v-else
          class="grade-avaliacoes"
      >
        <Card
            v-for="avaliacao in avaliacoes"
            :key="avaliacao.id"
      >
          <template #title>
            Avaliação #{{ avaliacao.id }}
          </template>

          <template #subtitle>
            {{ formatarData(avaliacao.dataHora) }}
          </template>

          <template #content>
            <Rating
                :model-value="avaliacao.nota"
                :cancel="false"
                readonly
            />

            <p class="comentario-avaliacao">
              {{ avaliacao.comentario }}
            </p>
          </template>

          <template #footer>
            <div class="acoes-card">
              <Button
                  label="Editar"
                  icon="pi pi-pencil"
                  severity="warn"
                  @click="editar(avaliacao)"
              />

              <Button
                  label="Excluir"
                  icon="pi pi-trash"
                  severity="danger"
                  @click="excluir(avaliacao.id)"
              />
            </div>
          </template>
        </Card>
      </div>
    </section>
  </main>
</template>