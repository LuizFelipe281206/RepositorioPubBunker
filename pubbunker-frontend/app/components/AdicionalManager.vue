<script setup>
import { CATEGORIAS, LIMITES, normalizarNome, chaveNome, precoValido, mensagemErro } from '~/utils/catalogo.js'
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

const categoriaFiltrada = ref('Todas')
const busca = ref('')
const categoriasFiltro = computed(() => ['Todas', ...CATEGORIAS,
  ...(adicionais.value.some(a => !CATEGORIAS.includes(a.categoria)) ? ['A classificar'] : [])])
const adicionaisExibidos = computed(() => adicionais.value.filter(a =>
  (categoriaFiltrada.value === 'Todas' || (CATEGORIAS.includes(a.categoria) ? a.categoria : 'A classificar') === categoriaFiltrada.value)
  && chaveNome(a.nome).includes(chaveNome(busca.value))))

const formularioVazio = () => ({
  nome: '',
  preco: null,
  categoria: '',
  ativo: true
})

const form = reactive(formularioVazio())

const adicionalEditandoId = ref(null)
const adicionalParaExcluir = ref(null)
const confirmacaoVisivel = ref(false)
const salvando = ref(false)
const excluindo = ref(false)

onMounted(async () => {
  try { await carregarAdicionais() }
  catch (erro) { abrirPopup('Não foi possível carregar os adicionais', mensagemErro(erro, 'Tente novamente em instantes.'), 'erro') }
})

const limparFormulario = () => {
  Object.assign(form, formularioVazio())
  adicionalEditandoId.value = null
}

const salvar = async () => {
  if (salvando.value || carregandoAdicionais.value) return
  const nome = normalizarNome(form.nome)
  let problema = ''
  if (!nome || form.preco === null || !form.categoria) problema = 'Preencha nome, preço e categoria do adicional.'
  else if (nome.length > LIMITES.nome) problema = 'O nome deve possuir no máximo 100 caracteres.'
  else if (!CATEGORIAS.includes(form.categoria)) problema = 'Selecione Bebidas, Porções ou Lanches.'
  else if (!precoValido(form.preco, 0, LIMITES.adicionalCentavos)) problema = 'O preço do adicional deve estar entre R$ 0,00 e R$ 100,00, com até duas casas decimais.'
  else if (adicionais.value.some(a => a.id !== adicionalEditandoId.value && chaveNome(a.nome) === chaveNome(nome)))
    problema = 'Já existe um adicional com este nome.'
  if (problema) { abrirPopup('Confira o adicional', problema, 'erro'); return }

  salvando.value = true

  try {
    await salvarAdicional(
      {
        nome,
        categoria: form.categoria,
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
    }
  } catch (erro) {
    abrirPopup(
      'Não foi possível salvar o adicional',
      mensagemErro(erro, 'Confira os dados do adicional e tente novamente.'),
      'erro'
    )
  } finally {
    salvando.value = false
  }
}

const editar = (adicional) => {
  adicionalEditandoId.value = adicional.id

  form.categoria = adicional.categoria || ''
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

  const id = adicionalParaExcluir.value.id
  excluindo.value = true

  try {
    await excluirAdicional(id)

    abrirPopup(
      'Adicional arquivado',
      'O adicional foi removido com sucesso.'
    )

    if (
      adicionalEditandoId.value === id
    ) {
      limparFormulario()
    }

    if (adicionalParaExcluir.value?.id === id) {
      cancelarExclusao()
    }
  } catch (erro) {
    abrirPopup(
      'Não foi possível arquivar o adicional',
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
  v-model:categoria="form.categoria"
  v-model:ativo="form.ativo"
  :salvando="salvando"
  :carregando="carregandoAdicionais"
  @salvar="salvar"
/>

      <div v-if="props.mostrarLista" class="filtros-setor">
        <label for="categoria-filtro-adicionais">Categoria dos adicionais</label>
        <Select input-id="categoria-filtro-adicionais" v-model="categoriaFiltrada" :options="categoriasFiltro" />
        <InputText v-model="busca" aria-label="Buscar adicional" placeholder="Buscar adicional" maxlength="100" />
        <small class="texto-secundario">{{ adicionaisExibidos.length }} adicionais encontrados.</small>
        <small v-if="adicionais.some(a => !CATEGORIAS.includes(a.categoria))" class="texto-secundario">
          Use Editar para classificar os adicionais antigos em Bebidas, Porções ou Lanches.
        </small>
      </div>

      <div v-if="props.mostrarLista" class="lista-adicionais">

        <p
          v-if="carregandoAdicionais"
          class="texto-secundario"
        >
          Carregando adicionais...
        </p>

        <p
          v-else-if="!adicionaisExibidos.length"
          class="texto-secundario"
        >
          Nenhum adicional cadastrado.
        </p>

        <div
          v-else
          class="grade-adicionais setor-rolavel" role="region" aria-label="Adicionais cadastrados" tabindex="0"
        >
          <article
            v-for="adicional in adicionaisExibidos"
            :key="adicional.id"
            class="adicional-item"
          >
            <div class="adicional-informacoes">
              <div>
                <strong>{{ adicional.nome }}</strong>
                <small class="texto-secundario">{{ adicional.categoria || 'A classificar' }}</small>

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
  :closable="false"
  :close-on-escape="!salvando"
  :style="{ width: 'min(92vw, 420px)' }"
  @after-hide="limparFormulario"
>
  <AdicionalForm
    v-if="adicionalEditandoId !== null"
    v-model:nome="form.nome"
    v-model:preco="form.preco"
  v-model:categoria="form.categoria"
    v-model:ativo="form.ativo"
    editando
    :salvando="salvando"
  :carregando="carregandoAdicionais"
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
  grid-auto-rows: minmax(5.75rem, auto);
}

.grade-adicionais.setor-rolavel {
  max-block-size: min(calc(4 * 5.75rem + 3 * 10px + .75rem), 62vh);
  max-block-size: min(calc(4 * 5.75rem + 3 * 10px + .75rem), 62dvh);
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
