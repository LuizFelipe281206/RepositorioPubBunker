<script setup>
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
  Object.assign(
      form,
      formularioVazio()
  )

  adicionalEditandoId.value = null
}

const salvar = async () => {
  if (
      !form.nome.trim() ||
      form.preco === null
  ) {
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

    limparFormulario()
  } catch (erro) {
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
}

const solicitarExclusao = (adicional) => {
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
    await excluirAdicional(
        adicionalParaExcluir.value.id
    )

    abrirPopup(
        'Adicional arquivado',
        'O adicional foi removido com sucesso.'
    )

    if (
        adicionalEditandoId.value ===
        adicionalParaExcluir.value.id
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
      Gerenciar adicionais
    </template>

    <template #content>
      <form
          class="form-adicional"
          @submit.prevent="salvar"
      >
        <div class="campo">
          <label for="nome-adicional">
            Nome
            <span class="campo-obrigatorio">*</span>
          </label>

          <InputText
              id="nome-adicional"
              v-model="form.nome"
              maxlength="100"
              placeholder="Ex.: Bacon extra"
              fluid
          />
        </div>

        <div class="campo">
          <label for="preco-adicional">
            Preço
            <span class="campo-obrigatorio">*</span>
          </label>

          <InputNumber
              input-id="preco-adicional"
              v-model="form.preco"
              mode="currency"
              currency="BRL"
              locale="pt-BR"
              :min="0"
              fluid
          />
        </div>

        <div class="campo checkbox-campo">
          <Checkbox
              v-model="form.ativo"
              input-id="adicional-ativo"
              binary
          />

          <label for="adicional-ativo">
            Adicional ativo
          </label>
        </div>

        <div class="acoes-adicional-form">
          <Button
              type="submit"
              :label="
                adicionalEditandoId
                    ? 'Atualizar adicional'
                    : 'Cadastrar adicional'
              "
              icon="pi pi-save"
              :loading="salvando"
          />

          <Button
              v-if="adicionalEditandoId"
              type="button"
              label="Cancelar"
              severity="secondary"
              @click="limparFormulario"
          />
        </div>
      </form>

      <div class="lista-adicionais">
        <h3>Adicionais cadastrados</h3>

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
                  :value="
                    adicional.ativo
                        ? 'Ativo'
                        : 'Inativo'
                  "
                  :severity="
                    adicional.ativo
                        ? 'success'
                        : 'secondary'
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
                  @click="
                    solicitarExclusao(adicional)
                  "
              />
            </div>
          </article>
        </div>
      </div>
    </template>
  </Card>

  <Dialog
      v-model:visible="confirmacaoVisivel"
      modal
      header="Arquivar adicional"
      :style="{ width: 'min(92vw, 420px)' }"
      @hide="adicionalParaExcluir = null"
  >
    <p v-if="adicionalParaExcluir">
      Tem certeza que deseja arquivar
      <strong>
        {{ adicionalParaExcluir.nome }}
      </strong>?
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