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
const { abrirPopup } = usePopup()

const formularioVazio = () => ({
  nome: '',
  descricao: '',
  preco: null,
  categoria: '',
  ativo: true
})

const form = reactive(formularioVazio())

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
                ativo: produto.ativo
              }
              : formularioVazio()
      )
    },
    {
      immediate: true
    }
)

const salvar = async () => {
  if (
      !form.nome ||
      !form.descricao ||
      !form.preco ||
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
          ...form,
          preco: Number(form.preco)
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
  } catch {
    abrirPopup(
        'Erro',
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
          <label for="nome">Nome</label>

          <InputText
              id="nome"
              v-model="form.nome"
              fluid
          />
        </div>

        <div class="campo campo-largo">
          <label for="descricao">
            Descrição
          </label>

          <InputText
              id="descricao"
              v-model="form.descricao"
              fluid
          />
        </div>

        <div class="campo">
          <label for="preco">Preço</label>

          <InputNumber
              id="preco"
              v-model="form.preco"
              mode="currency"
              currency="BRL"
              locale="pt-BR"
              fluid
          />
        </div>

        <div class="campo">
          <label for="categoria">
            Categoria
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