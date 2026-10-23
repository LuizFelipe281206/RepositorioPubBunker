<script setup>
import { CATEGORIAS, LIMITES, normalizarNome, chaveNome, precoValido, mensagemErro, agruparAdicionais } from '~/utils/catalogo.js'

const props = defineProps({ produto: { type: Object, default: null } })
const emit = defineEmits(['salvo', 'cancelado'])
const { produtos, salvarProduto, carregarProdutos } = useProdutos()
const { adicionais, carregandoAdicionais, carregarAdicionais } = useAdicionais()
const { abrirPopup } = usePopup()
const salvando = ref(false)
const formularioVazio = () => ({ nome: '', descricao: '', preco: null, categoria: '', ativo: true, adicionaisIds: [] })
const form = reactive(formularioVazio())
const gruposAdicionais = computed(() => agruparAdicionais(adicionais.value.filter(a => a.ativo)))

onMounted(async () => {
  try {
    await Promise.all([
      adicionais.value.length ? null : carregarAdicionais(),
      produtos.value.length ? null : carregarProdutos()
    ])
  } catch (erro) {
    abrirPopup('Não foi possível carregar o cadastro', mensagemErro(erro, 'Tente carregar os adicionais novamente.'), 'erro')
  }
})
watch(() => props.produto, produto => {
  Object.assign(form, produto ? {
    nome: produto.nome, descricao: produto.descricao, preco: Number(produto.preco),
    categoria: produto.categoria, ativo: produto.ativo,
    adicionaisIds: produto.adicionaisDisponiveis?.filter(a => a.ativo)?.map(a => a.id) || []
  } : formularioVazio())
}, { immediate: true })

const formatarPreco = valor => Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const salvar = async () => {
  if (salvando.value || carregandoAdicionais.value) return
  const dados = { nome: normalizarNome(form.nome), descricao: form.descricao.trim(), preco: Number(form.preco),
    categoria: form.categoria, ativo: form.ativo, adicionaisIds: [...form.adicionaisIds] }
  let problema = ''
  if (!dados.nome || !dados.descricao || !dados.categoria || form.preco === null)
    problema = 'Preencha nome, descrição, preço e categoria.'
  else if (dados.nome.length > LIMITES.nome) problema = 'O nome deve possuir no máximo 100 caracteres.'
  else if (dados.descricao.length > LIMITES.descricao) problema = 'A descrição deve possuir no máximo 500 caracteres.'
  else if (!CATEGORIAS.includes(dados.categoria)) problema = 'Selecione Bebidas, Porções ou Lanches.'
  else if (!precoValido(form.preco, 1, LIMITES.produtoCentavos))
    problema = 'O preço base deve estar entre R$ 0,01 e R$ 1.000,00, com até duas casas decimais.'
  else if (produtos.value.some(p => p.id !== props.produto?.id && chaveNome(p.nome) === chaveNome(dados.nome)))
    problema = 'Já existe um produto com este nome.'
  if (problema) { abrirPopup('Confira o produto', problema, 'erro'); return }
  salvando.value = true
  try {
    await salvarProduto(dados, props.produto?.id)
    abrirPopup(props.produto ? 'Produto atualizado' : 'Produto cadastrado',
      props.produto ? 'O produto foi atualizado com sucesso.' : 'O produto foi cadastrado com sucesso.')
    Object.assign(form, formularioVazio())
    emit('salvo')
  } catch (erro) {
    abrirPopup('Não foi possível salvar o produto', mensagemErro(erro, 'Confira os dados do produto e tente novamente.'),
      'erro', erro.response?.status === 429 ? 60 : null)
  } finally { salvando.value = false }
}
</script>

<template>
  <Card class="painel">
    <template #title>{{ produto ? 'Editar produto' : 'Cadastrar produto' }}</template>
    <template #content>
      <form class="form-grid formulario-produto" @submit.prevent="salvar">
        <div class="campo campo-largo">
          <label for="nome-produto">Nome <span class="campo-obrigatorio">*</span></label>
          <InputText id="nome-produto" v-model="form.nome" maxlength="100" :disabled="salvando" required fluid />
        </div>
        <div class="campo campo-largo">
          <label for="descricao-produto">Descrição <span class="campo-obrigatorio">*</span></label>
          <Textarea id="descricao-produto" v-model="form.descricao" maxlength="500" rows="3" :disabled="salvando" required fluid />
          <small class="texto-secundario">{{ form.descricao.length }}/500 caracteres</small>
        </div>
        <div class="campo">
          <label for="preco-produto">Preço base <span class="campo-obrigatorio">*</span></label>
          <InputNumber input-id="preco-produto" v-model="form.preco" mode="currency" currency="BRL" locale="pt-BR"
            :min="0.01" :max="1000" :max-fraction-digits="2" :disabled="salvando" fluid />
        </div>
        <div class="campo">
          <label for="categoria-produto">Categoria <span class="campo-obrigatorio">*</span></label>
          <Select input-id="categoria-produto" v-model="form.categoria" :options="CATEGORIAS" :disabled="salvando" placeholder="Selecione" fluid />
        </div>
        <div class="campo campo-largo">
          <label for="adicionais-produto">Adicionais disponíveis</label>
          <MultiSelect input-id="adicionais-produto" v-model="form.adicionaisIds" :options="gruposAdicionais"
            option-group-label="label" option-group-children="items" option-label="nome" option-value="id"
            filter scroll-height="240px" :disabled="salvando"
            :loading="carregandoAdicionais" placeholder="Selecione os adicionais disponíveis"
            empty-message="Nenhum adicional cadastrado" empty-filter-message="Nenhum adicional encontrado" fluid>
            <template #value="{ value, placeholder }">
              <span v-if="value?.length">{{ value.length }} {{ value.length === 1 ? 'adicional vinculado' : 'adicionais vinculados' }}</span>
              <span v-else>{{ placeholder }}</span>
            </template>
            <template #option="{ option }">
              <div class="opcao-adicional-produto"><span :title="option.nome">{{ option.nome }}</span><small>+ {{ formatarPreco(option.preco) }}</small></div>
            </template>
          </MultiSelect>
        </div>
        <div class="campo checkbox-campo">
          <Checkbox v-model="form.ativo" input-id="produto-ativo" :disabled="salvando" binary />
          <label for="produto-ativo">Produto ativo</label>
        </div>
      </form>
    </template>
    <template #footer>
      <div class="acoes-card">
        <Button :label="produto ? 'Atualizar' : 'Cadastrar'" icon="pi pi-save" :loading="salvando"
          :disabled="salvando || carregandoAdicionais" @click="salvar" />
        <Button v-if="produto" label="Cancelar" severity="secondary" :disabled="salvando" @click="emit('cancelado')" />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.formulario-produto .campo:not(.checkbox-campo) { display:flex; flex-direction:column; align-items:stretch; min-width:0; }
.opcao-adicional-produto { display:flex; justify-content:space-between; gap:16px; min-width:0; width:100%; }
.opcao-adicional-produto span { overflow-wrap:anywhere; }
.opcao-adicional-produto small { color:var(--bunker-muted); white-space:nowrap; }
:deep(.p-multiselect-label) { display:flex; flex-wrap:wrap; gap:4px; min-width:0; }
:deep(.p-chip) { max-width:100%; }
:deep(.p-chip-label) { overflow:hidden; text-overflow:ellipsis; max-width:18rem; }
</style>
