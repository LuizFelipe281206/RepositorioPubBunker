<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const API_URL = 'http://localhost:8080/produtos'

const produtos = ref([])
const editando = ref(false)
const idEditando = ref(null)

const form = ref({
  nome: '',
  descricao: '',
  preco: '',
  categoria: '',
  ativo: true
})

const carregarProdutos = async () => {
  try {
    const response = await axios.get(API_URL)
    produtos.value = response.data
  } catch (error) {
    console.error('Erro ao carregar produtos:', error)
  }
}

const limparFormulario = () => {
  form.value = {
    nome: '',
    descricao: '',
    preco: '',
    categoria: '',
    ativo: true
  }
  editando.value = false
  idEditando.value = null
}

const salvarProduto = async () => {
  try {
    const payload = {
      nome: form.value.nome,
      descricao: form.value.descricao,
      preco: Number(form.value.preco),
      categoria: form.value.categoria,
      ativo: form.value.ativo
    }

    if (editando.value) {
      await axios.put(`${API_URL}/${idEditando.value}`, payload)
    } else {
      await axios.post(API_URL, payload)
    }

    await carregarProdutos()
    limparFormulario()
  } catch (error) {
    console.error('Erro ao salvar produto:', error)
  }
}

const editarProduto = (produto) => {
  form.value = {
    nome: produto.nome,
    descricao: produto.descricao,
    preco: produto.preco,
    categoria: produto.categoria,
    ativo: produto.ativo
  }
  editando.value = true
  idEditando.value = produto.id
}

const excluirProduto = async (id) => {
  try {
    await axios.delete(`${API_URL}/${id}`)
    await carregarProdutos()
  } catch (error) {
    console.error('Erro ao excluir produto:', error)
  }
}

onMounted(() => {
  carregarProdutos()
})
</script>

<template>
  <div class="container">
    <h1>Gerenciar Cardápio</h1>

    <div class="formulario">
      <input v-model="form.nome" type="text" placeholder="Nome do produto" />
      <input v-model="form.descricao" type="text" placeholder="Descrição" />
      <input v-model="form.preco" type="number" step="0.01" placeholder="Preço" />
      <input v-model="form.categoria" type="text" placeholder="Categoria" />

      <label class="checkbox">
        <input v-model="form.ativo" type="checkbox" />
        Ativo
      </label>

      <div class="acoes-form">
        <button @click="salvarProduto">
          {{ editando ? 'Atualizar Produto' : 'Cadastrar Produto' }}
        </button>

        <button v-if="editando" class="btn-cancelar" @click="limparFormulario">
          Cancelar
        </button>
      </div>
    </div>

    <div class="lista">
      <div class="card" v-for="produto in produtos" :key="produto.id">
        <h2>{{ produto.nome }}</h2>
        <p><strong>Descrição:</strong> {{ produto.descricao }}</p>
        <p><strong>Preço:</strong> R$ {{ produto.preco }}</p>
        <p><strong>Categoria:</strong> {{ produto.categoria }}</p>
        <p><strong>Ativo:</strong> {{ produto.ativo ? 'Sim' : 'Não' }}</p>

        <div class="acoes-card">
          <button class="btn-editar" @click="editarProduto(produto)">Editar</button>
          <button class="btn-excluir" @click="excluirProduto(produto.id)">Excluir</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: Arial, sans-serif;
  background: #f4f4f4;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

h1 {
  text-align: center;
  margin-bottom: 24px;
}

.formulario {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.formulario input[type="text"],
.formulario input[type="number"] {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.acoes-form {
  display: flex;
  gap: 10px;
}

button {
  border: none;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
}

.btn-cancelar {
  background: #999;
  color: white;
}

.lista {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.card {
  background: white;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card h2 {
  margin-top: 0;
}

.acoes-card {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.btn-editar {
  background: #f0ad4e;
  color: white;
}

.btn-excluir {
  background: #d9534f;
  color: white;
}
</style>