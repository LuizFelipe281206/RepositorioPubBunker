<script setup>
const props = defineProps({
  produto: {
    type: Object,
    default: null
  }
})

const emit = defineEmits([
  'salvo',
  'cancelado'
])

const { salvarProduto } = useProdutos()

const {
  adicionais,
  carregandoAdicionais,
  carregarAdicionais
} = useAdicionais()

const { abrirPopup } = usePopup()

const formularioVazio = () => ({
  nome: '',
  descricao: '',
  preco: null,
  categoria: '',
  ativo: true,
  adicionaisIds: []
})

const form = reactive(formularioVazio())

onMounted(async () => {
  if (!adicionais.value.length) {
    await carregarAdicionais()
  }
})

watch(
    () => props.produto,
    produto => {
      Object.assign(
          form,
          produto
              ? {
                nome: produto.nome,
                descricao: produto.descricao,
                preco: Number(produto.preco),
                categoria: produto.categoria,
                ativo: produto.ativo,
                adicionaisIds:
                    produto
                        .adicionaisDisponiveis
                        ?.map(
                            adicional =>
                                adicional.id
                        ) || []
              }
              : formularioVazio()
      )
    },
    {
      immediate: true
    }
)

const formatarPreco = valor =>
    Number(valor).toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })

const salvar = async () => {
  if (
      !form.nome.trim() ||
      !form.descricao.trim() ||
      form.preco === null ||
      !form.categoria
  ) {
    abrirPopup(
        'Campos obrigatórios',
        'Preencha nome, descrição, preço e categoria.',
        'erro'
    )

    return
  }

  if (Number(form.preco) <= 0) {
    abrirPopup(
        'Preço inválido',
        'O preço deve ser maior que zero.',
        'erro'
    )

    return
  }

  try {
    await salvarProduto(
        {
          nome: form.nome.trim(),
          descricao: form.descricao.trim(),
          preco: Number(form.preco),
          categoria: form.categoria,
          ativo: form.ativo,
          adicionaisIds: [
            ...form.adicionaisIds
          ]
        },
        props.produto?.id
    )

    abrirPopup(
        props.produto
            ? 'Produto atualizado'
            : 'Produto cadastrado',
        `O produto foi ${
            props.produto
                ? 'atualizado'
                : 'cadastrado'
        } com sucesso.`
    )

    Object.assign(
        form,
        formularioVazio()
    )

    emit('salvo')
  } catch (erro) {
    abrirPopup(
        'Erro',
        erro.response?.data?.mensagem ||
        'Não foi possível salvar o produto.',
        'erro'
    )
  }
}
</script>

<template>
  <Card class="painel">
    <template #title>
      {{
        produto
            ? 'Editar produto'
            : 'Cadastrar produto'
      }}
    </template>

    <template #content>
      <div class="form-grid">
        <div class="campo campo-largo">
          <label for="nome">
            Nome
            <span class="campo-obrigatorio">*</span>
          </label>

          <InputText
              id="nome"
              v-model="form.nome"
              maxlength="100"
              fluid
          />
        </div>

        <div class="campo campo-largo">
          <label for="descricao">
            Descrição
            <span class="campo-obrigatorio">*</span>
          </label>

          <InputText
              id="descricao"
              v-model="form.descricao"
              maxlength="500"
              fluid
          />
        </div>

        <div class="campo">
          <label for="preco">
            Preço
            <span class="campo-obrigatorio">*</span>
          </label>

          <InputNumber
              input-id="preco"
              v-model="form.preco"
              mode="currency"
              currency="BRL"
              locale="pt-BR"
              :min="0.01"
              fluid
          />
        </div>

        <div class="campo">
          <label for="categoria">
            Categoria
            <span class="campo-obrigatorio">*</span>
          </label>

          <Select
              id="categoria"
              v-model="form.categoria"
              :options="[
                'Bebidas',
                'Porções',
                'Lanches'
              ]"
              placeholder="Selecione"
              fluid
          />
        </div>

        <div class="campo campo-largo">
          <label for="adicionais-produto">
            Adicionais disponíveis
          </label>

          <MultiSelect
              input-id="adicionais-produto"
              v-model="form.adicionaisIds"
              :options="adicionais"
              option-label="nome"
              option-value="id"
              display="chip"
              filter
              :loading="carregandoAdicionais"
              placeholder="Selecione os adicionais"
              empty-message="Nenhum adicional cadastrado"
              empty-filter-message="Nenhum adicional encontrado"
              fluid
          >
            <template #option="{ option }">
              <div class="opcao-adicional-produto">
                <span>{{ option.nome }}</span>

                <small>
                  + {{ formatarPreco(option.preco) }}
                </small>
              </div>
            </template>
          </MultiSelect>

          <small class="texto-secundario">
            Os adicionais selecionados poderão ser
            escolhidos pelo cliente neste produto.
          </small>
        </div>

        <div class="campo checkbox-campo">
          <Checkbox
              v-model="form.ativo"
              input-id="ativo"
              binary
          />

          <label for="ativo">
            Produto ativo
          </label>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="acoes-card">
        <Button
            :label="
              produto
                  ? 'Atualizar'
                  : 'Cadastrar'
            "
            icon="pi pi-save"
            @click="salvar"
        />

        <Button
            v-if="produto"
            label="Cancelar"
            severity="secondary"
            @click="$emit('cancelado')"
        />
      </div>
    </template>
  </Card>
</template>

<style scoped>
.opcao-adicional-produto {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  width: 100%;
}

.opcao-adicional-produto small {
  color: var(--bunker-muted);
}
</style>