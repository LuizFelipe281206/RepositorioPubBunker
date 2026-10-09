<script setup>
const props = defineProps({
  mostrarFormulario: {
    type: Boolean,
    default: true
  },
  mostrarLista: {
    type: Boolean,
    default: false
  }
})

const edicaoVisivel = ref(false)
const {
  adicionais,
  carregandoAdicionais,
  carregarAdicionais,
  salvarAdicional,
  excluirAdicional
} = useAdicionais()

const { abrirPopup } = usePopup()

const formularioVazio = () => ({
  nome: '',
  preco: null,
  ativo: true
})

const form = reactive(formularioVazio())

const adicionalEditandoId = ref(null)
const adicionalParaExcluir = ref(null)
const confirmacaoVisivel = ref(false)
const salvando = ref(false)
const excluindo = ref(false)

onMounted(carregarAdicionais)

const limparFormulario = () => {
  Object.assign(form, formularioVazio())
  adicionalEditandoId.value = null
}

const salvar = async () => {
  if (!form.nome.trim() || form.preco === null) {
    abrirPopup(
      'Campos obrigatórios',
      'Preencha o nome e o preço do adicional.',
      'erro'
    )
    return
  }

  if (Number(form.preco) < 0) {
    abrirPopup(
      'Preço inválido',
      'O preço do adicional não pode ser negativo.',
      'erro'
    )
    return
  }

  salvando.value = true

  try {
    await salvarAdicional(
      {
        nome: form.nome.trim(),
        preco: Number(form.preco),
        ativo: form.ativo
      },
      adicionalEditandoId.value
    )

    abrirPopup(
      adicionalEditandoId.value
        ? 'Adicional atualizado'
        : 'Adicional cadastrado',
      adicionalEditandoId.value
        ? 'O adicional foi atualizado com sucesso.'
        : 'O adicional foi cadastrado com sucesso.'
    )

if (edicaoVisivel.value) {
  edicaoVisivel.value = false
} else {
  limparFormulario()
}  } catch (erro) {
    abrirPopup(
      'Erro',
      erro.response?.data?.mensagem ||
        'Não foi possível salvar o adicional.',
      'erro'
    )
  } finally {
    salvando.value = false
  }
}

const editar = (adicional) => {
  adicionalEditandoId.value = adicional.id

  form.nome = adicional.nome
  form.preco = Number(adicional.preco)
  form.ativo = adicional.ativo

  edicaoVisivel.value = true
}

const solicitarExclusao = (adicional) => {
  if (adicional.ativo) {
    abrirPopup(
      'Ação não permitida',
      'Inative o adicional antes de arquivá-lo.',
      'erro'
    )
    return
  }

  adicionalParaExcluir.value = adicional
  confirmacaoVisivel.value = true
}

const cancelarExclusao = () => {
  confirmacaoVisivel.value = false
  adicionalParaExcluir.value = null
}

const confirmarExclusao = async () => {
  if (!adicionalParaExcluir.value) {
    return
  }

  excluindo.value = true

  try {
    await excluirAdicional(adicionalParaExcluir.value.id)

    abrirPopup(
      'Adicional arquivado',
      'O adicional foi removido com sucesso.'
    )

    if (
      adicionalEditandoId.value === adicionalParaExcluir.value.id
    ) {
      limparFormulario()
    }

    cancelarExclusao()
  } catch (erro) {
    abrirPopup(
      'Erro',
      erro.response?.data?.mensagem ||
        'Não foi possível arquivar o adicional.',
      'erro'
    )
  } finally {
    excluindo.value = false
  }
}

const formatarPreco = valor =>
  Number(valor).toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  })
</script>

<template>
  <Card class="painel">
    <template #title>
{{
  props.mostrarFormulario
    ? 'Cadastrar adicional'
    : 'Adicionais cadastrados'
}}    </template>

    <template #content>
      <AdicionalForm
  v-if="props.mostrarFormulario"
  v-model:nome="form.nome"
  v-model:preco="form.preco"
  v-model:ativo="form.ativo"
  :salvando="salvando"
  @salvar="salvar"
/>

      <div v-if="props.mostrarLista" class="lista-adicionais">

        <p
          v-if="carregandoAdicionais"
          class="texto-secundario"
        >
          Carregando adicionais...
        </p>

        <p
          v-else-if="!adicionais.length"
          class="texto-secundario"
        >
          Nenhum adicional cadastrado.
        </p>

        <div
          v-else
          class="grade-adicionais"
        >
          <article
            v-for="adicional in adicionais"
            :key="adicional.id"
            class="adicional-item"
          >
            <div class="adicional-informacoes">
              <div>
                <strong>{{ adicional.nome }}</strong>

                <span>
                  {{ formatarPreco(adicional.preco) }}
                </span>
              </div>

              <Tag
                :value="adicional.ativo ? 'Ativo' : 'Inativo'"
                :severity="
                  adicional.ativo ? 'success' : 'secondary'
                "
              />
            </div>

            <div class="acoes-adicional">
              <Button
                label="Editar"
                icon="pi pi-pencil"
                severity="secondary"
                outlined
                @click="editar(adicional)"
              />

              <Button
                label="Arquivar"
                icon="pi pi-trash"
                severity="danger"
                outlined
                :disabled="adicional.ativo"
                :title="
                  adicional.ativo
                    ? 'Inative o adicional antes de arquivá-lo.'
                    : 'Arquivar adicional'
                "
                @click="solicitarExclusao(adicional)"
              />
            </div>
          </article>
        </div>
      </div>
    </template>
  </Card>
<Dialog
  v-model:visible="edicaoVisivel"
  modal
  header="Editar adicional"
  :draggable="false"
  :closable="!salvando"
  :close-on-escape="!salvando"
  :style="{ width: 'min(92vw, 420px)' }"
  @after-hide="limparFormulario"
>
  <AdicionalForm
    v-if="adicionalEditandoId !== null"
    v-model:nome="form.nome"
    v-model:preco="form.preco"
    v-model:ativo="form.ativo"
    editando
    :salvando="salvando"
    @salvar="salvar"
    @cancelar="edicaoVisivel = false"
  />
</Dialog>
  <Dialog
    v-model:visible="confirmacaoVisivel"
    modal
    header="Arquivar adicional"
    :style="{ width: 'min(92vw, 420px)' }"
    @hide="adicionalParaExcluir = null"
  >
    <p v-if="adicionalParaExcluir">
      Tem certeza que deseja arquivar
      <strong>{{ adicionalParaExcluir.nome }}</strong>?
    </p>

    <p class="texto-secundario">
      Ele não poderá ser escolhido em novos pedidos.
    </p>

    <template #footer>
      <Button
        label="Cancelar"
        severity="secondary"
        text
        @click="cancelarExclusao"
      />

      <Button
        label="Arquivar"
        icon="pi pi-trash"
        severity="danger"
        :loading="excluindo"
        @click="confirmarExclusao"
      />
    </template>
  </Dialog>
</template>

<style scoped>
.form-adicional {
  display: grid;
  grid-template-columns: minmax(180px, 2fr) minmax(150px, 1fr);
  align-items: end;
  gap: 16px;
}

.checkbox-campo,
.acoes-adicional-form {
  grid-column: 1 / -1;
}

.acoes-adicional-form,
.acoes-adicional {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.lista-adicionais {
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px solid var(--bunker-border);
}

.lista-adicionais h3 {
  margin: 0 0 16px;
}

.grade-adicionais {
  display: grid;
  gap: 10px;
}

.adicional-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px;
  background: var(--bunker-bg);
  border: 1px solid var(--bunker-border);
  border-radius: 8px;
}

.adicional-informacoes {
  display: flex;
  align-items: center;
  gap: 14px;
}

.adicional-informacoes > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.adicional-informacoes span {
  color: var(--bunker-muted);
}

@media (max-width: 600px) {
  .form-adicional {
    grid-template-columns: 1fr;
  }

  .checkbox-campo,
  .acoes-adicional-form {
    grid-column: auto;
  }

  .adicional-item {
    align-items: stretch;
    flex-direction: column;
  }

  .adicional-informacoes {
    justify-content: space-between;
  }

  .acoes-adicional {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}
</style>