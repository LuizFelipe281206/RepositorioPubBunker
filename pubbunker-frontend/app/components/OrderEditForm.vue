<script setup>
import { LIMITES, mensagemErro } from '~/utils/catalogo.js'
import { criarRascunhoPedido, opcoesAdicionaisEdicao, calcularLinhaEdicao, conferirEdicaoPedido, dadosEdicaoPedido } from '~/utils/edicaoPedido.js'

const props = defineProps({ pedidoId: { type: Number, required: true }, salvando: Boolean })
const emit = defineEmits(['salvar', 'cancelar'])
const { $api } = useNuxtApp()
const carregando = ref(true)
const erroCarregamento = ref('')
const produtos = ref([])
const rascunho = ref(null)
const produtoParaAdicionar = ref(null)
const busca = ref('')
let ativo = true
let sequencia = 0

const produtosDisponiveis = computed(() => produtos.value.filter(produto => produto.ativo))
const conferido = computed(() => rascunho.value ? conferirEdicaoPedido(rascunho.value, produtos.value) : { total: 0, erro: '' })
const itensExibidos = computed(() => rascunho.value?.itens.filter(item =>
  item.nome.toLocaleLowerCase('pt-BR').includes(busca.value.trim().toLocaleLowerCase('pt-BR'))) || [])
const formatarPreco = centavos => (centavos / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
const subtotal = item => {
  try { return formatarPreco(calcularLinhaEdicao(item, produtos.value).subtotal) }
  catch { return 'Confira este item' }
}
const carregar = async () => {
  if (props.salvando) return
  carregando.value = true
  erroCarregamento.value = ''
  try {
    const [pedido, catalogo] = await Promise.all([$api.get(`/pedidos/${props.pedidoId}`), $api.get('/produtos')])
    if (!ativo) return
    if (!['PENDENTE', 'EM_PREPARO'].includes(pedido.data.status)) {
      erroCarregamento.value = 'Só é possível editar pedidos pendentes ou em preparo.'
      return
    }
    produtos.value = Array.isArray(catalogo.data) ? catalogo.data : []
    rascunho.value = criarRascunhoPedido(pedido.data)
  } catch (erro) {
    if (ativo) erroCarregamento.value = mensagemErro(erro, 'Não foi possível carregar o pedido. Tente novamente.')
  } finally { if (ativo) carregando.value = false }
}
const adicionarProduto = () => {
  if (props.salvando || !rascunho.value || rascunho.value.itens.length >= LIMITES.itens) return
  const produto = produtosDisponiveis.value.find(produto => produto.id === produtoParaAdicionar.value)
  if (!produto) return
  rascunho.value.itens.push({ chave: `novo-${++sequencia}`, itemId: null, produtoId: produto.id,
    nome: produto.nome, quantidade: 1, adicionaisIds: [], extrasOriginais: [] })
  produtoParaAdicionar.value = null
  busca.value = ''
}
const remover = item => {
  if (!props.salvando) rascunho.value.itens = rascunho.value.itens.filter(linha => linha.chave !== item.chave)
}
const salvar = () => {
  if (!props.salvando && !carregando.value && rascunho.value && !conferido.value.erro)
    emit('salvar', dadosEdicaoPedido(rascunho.value))
}
onMounted(carregar)
onBeforeUnmount(() => { ativo = false })
</script>

<template>
  <p v-if="carregando" role="status">Carregando pedido...</p>
  <div v-else-if="erroCarregamento" role="alert">
    <p>{{ erroCarregamento }}</p>
    <Button label="Tentar novamente" severity="secondary" :disabled="salvando" @click="carregar" />
    <Button label="Cancelar" severity="secondary" text @click="emit('cancelar')" />
  </div>
  <form v-else-if="rascunho" class="editor-pedido" @submit.prevent="salvar">
    <div class="adicionar-produto">
      <div class="campo">
        <label for="produto-edicao-pedido">Adicionar produto</label>
        <Select input-id="produto-edicao-pedido" v-model="produtoParaAdicionar" :options="produtosDisponiveis"
          option-label="nome" option-value="id" filter scroll-height="240px" placeholder="Selecione um produto"
          empty-message="Nenhum produto disponível" empty-filter-message="Nenhum produto encontrado" :disabled="salvando" fluid />
      </div>
      <Button label="Adicionar" icon="pi pi-plus" type="button" severity="secondary"
        :disabled="salvando || !produtoParaAdicionar || rascunho.itens.length >= LIMITES.itens" @click="adicionarProduto" />
    </div>
    <InputText v-if="rascunho.itens.length > 4" v-model="busca" aria-label="Buscar item do pedido" placeholder="Buscar item" :disabled="salvando" fluid />
    <div class="itens-edicao" role="region" aria-label="Itens do pedido em edição" tabindex="0">
      <article v-for="(item, indice) in itensExibidos" :key="item.chave" class="item-edicao">
        <div class="linha-item">
          <strong>{{ item.nome }}</strong>
          <Button icon="pi pi-trash" label="Remover" severity="danger" text type="button"
            :aria-label="`Remover ${item.nome}`" :disabled="salvando" @click="remover(item)" />
        </div>
        <div class="campos-item">
          <div class="campo">
            <label :for="`quantidade-edicao-${indice}`">Quantidade</label>
            <InputNumber :input-id="`quantidade-edicao-${indice}`" v-model="item.quantidade" show-buttons
              :min="1" :max="100" :max-fraction-digits="0" :disabled="salvando" fluid />
          </div>
          <div class="campo extras-item">
            <label :for="`extras-edicao-${indice}`">Adicionais</label>
            <MultiSelect :input-id="`extras-edicao-${indice}`" v-model="item.adicionaisIds"
              :options="opcoesAdicionaisEdicao(item, produtos)" option-group-label="label" option-group-children="items"
              option-label="nome" option-value="id" :selection-limit="4" :max-selected-labels="1" selected-items-label="{0} selecionados"
              filter scroll-height="220px" :disabled="salvando" placeholder="Selecione"
              empty-message="Nenhum adicional disponível" empty-filter-message="Nenhum adicional encontrado" fluid />
          </div>
        </div>
        <p class="subtotal-edicao">Subtotal: <strong>{{ subtotal(item) }}</strong></p>
      </article>
      <p v-if="!itensExibidos.length">{{ rascunho.itens.length ? 'Nenhum item encontrado.' : 'Nenhum produto no pedido.' }}</p>
    </div>
    <div class="campo">
      <label for="observacao-edicao-pedido">Observação</label>
      <Textarea id="observacao-edicao-pedido" v-model="rascunho.observacao" rows="3" maxlength="500" :disabled="salvando" fluid />
    </div>
    <p v-if="conferido.total !== null" class="total-edicao">Total: <strong>{{ formatarPreco(conferido.total) }}</strong></p>
    <p v-if="conferido.erro" class="erro-edicao" role="alert">{{ conferido.erro }}</p>
    <div class="acoes-editor">
      <Button label="Cancelar" severity="secondary" type="button" :disabled="salvando" @click="emit('cancelar')" />
      <Button label="Salvar alterações" icon="pi pi-save" type="submit" :loading="salvando" :disabled="salvando || Boolean(conferido.erro)" />
    </div>
  </form>
</template>

<style scoped>
.editor-pedido { display:grid; gap:16px; min-width:0; }
.campo { display:flex; flex-direction:column; gap:6px; min-width:0; }
.adicionar-produto { display:grid; grid-template-columns:minmax(0,1fr) auto; align-items:end; gap:10px; }
.itens-edicao { display:grid; gap:10px; max-height:24rem; max-height:min(24rem,45dvh); overflow-y:auto; padding:2px 8px 2px 2px; overscroll-behavior:contain; }
.item-edicao { border:1px solid var(--bunker-border); border-radius:8px; padding:12px; min-width:0; background:var(--bunker-cream); }
.linha-item { display:flex; align-items:center; justify-content:space-between; gap:8px; }
.linha-item strong { overflow-wrap:anywhere; }
.campos-item { display:grid; grid-template-columns:minmax(0,150px) minmax(0,1fr); gap:12px; }
.subtotal-edicao { margin:10px 0 0; text-align:right; }
.total-edicao { margin:0; text-align:right; font-size:1.15rem; }
.erro-edicao { color:var(--bunker-wine); margin:0; }
.acoes-editor { display:flex; justify-content:flex-end; gap:10px; flex-wrap:wrap; }
@media(max-width:520px) {
  .campos-item, .adicionar-produto { grid-template-columns:minmax(0,1fr); }
  .itens-edicao { max-height:min(14rem,28dvh); }
}
</style>
