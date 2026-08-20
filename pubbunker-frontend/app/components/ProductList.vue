<script setup>
defineProps({
  produtos: {
    type: Array,
    required: true
  },

  modoAdmin: {
    type: Boolean,
    default: false
  }
})

defineEmits([
  'adicionar',
  'editar',
  'excluir'
])

const formatarPreco = valor =>
    Number(valor).toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    })
</script>

<template>
  <div class="lista-produtos">
    <Card
        v-for="produto in produtos"
        :key="produto.id"
        class="produto-card"
    >
      <template #title>
        {{ produto.nome }}
      </template>

      <template #content>
        <p>{{ produto.descricao }}</p>

        <p>
          <strong>
            {{ formatarPreco(produto.preco) }}
          </strong>
        </p>

        <Tag
            :value="produto.categoria"
            severity="secondary"
        />
      </template>

      <template #footer>
        <div class="acoes-card">
          <template v-if="modoAdmin">
            <Button
                label="Editar"
                icon="pi pi-pencil"
                severity="warn"
                @click="$emit('editar', produto)"
            />

            <Button
                label="Excluir"
                icon="pi pi-trash"
                severity="danger"
                @click="$emit('excluir', produto.id)"
            />
          </template>

          <Button
              v-else
              label="Adicionar ao pedido"
              icon="pi pi-plus"
              severity="success"
              @click="$emit('adicionar', produto)"
          />
        </div>
      </template>
    </Card>
  </div>
</template>