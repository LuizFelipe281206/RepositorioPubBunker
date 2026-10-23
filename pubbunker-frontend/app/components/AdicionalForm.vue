<script setup>
import { CATEGORIAS } from '~/utils/catalogo.js'
const props = defineProps({
  editando: Boolean,
  salvando: Boolean,
  carregando: Boolean
})

const nome = defineModel('nome', {
  type: String,
  required: true
})

const preco = defineModel('preco', {
  type: Number,
  default: null
})

const categoria = defineModel('categoria', { type: String, default: '' })

const ativo = defineModel('ativo', {
  type: Boolean,
  required: true
})

defineEmits(['salvar', 'cancelar'])
</script>

<template>
  <form class="form-grid" @submit.prevent="$emit('salvar')">
    <div class="campo campo-largo">
      <label for="nome-adicional">
        Nome <span class="campo-obrigatorio">*</span>
      </label>

      <InputText
        id="nome-adicional"
        v-model="nome"
        maxlength="100"
        placeholder="Ex.: Bacon extra"
        :disabled="props.salvando || props.carregando"
        fluid
      />
    </div>

    <div class="campo">
      <label for="preco-adicional">
        Preço <span class="campo-obrigatorio">*</span>
      </label>

      <InputNumber
        input-id="preco-adicional"
        v-model="preco"
        mode="currency"
        currency="BRL"
        locale="pt-BR"
        :min="0"
        :max="100"
        :max-fraction-digits="2"
        :disabled="props.salvando || props.carregando"
        fluid
      />
    </div>

    <div class="campo">
      <label for="categoria-adicional">Categoria <span class="campo-obrigatorio">*</span></label>
      <Select input-id="categoria-adicional" v-model="categoria" :options="CATEGORIAS"
        placeholder="Selecione" :disabled="props.salvando || props.carregando" fluid />
    </div>

    <div class="campo checkbox-campo">
      <Checkbox
        v-model="ativo"
        input-id="adicional-ativo"
        :disabled="props.salvando || props.carregando"
        binary
      />

      <label for="adicional-ativo">Adicional ativo</label>
    </div>

    <div class="acoes-card campo-largo">
      <Button
        type="submit"
        :label="
          props.editando
            ? 'Atualizar adicional'
            : 'Cadastrar adicional'
        "
        icon="pi pi-save"
        :loading="props.salvando"
        :disabled="props.salvando || props.carregando"
      />

      <Button
        v-if="props.editando"
        type="button"
        label="Cancelar"
        severity="secondary"
        :disabled="props.salvando || props.carregando"
        @click="$emit('cancelar')"
      />
    </div>
  </form>
</template>