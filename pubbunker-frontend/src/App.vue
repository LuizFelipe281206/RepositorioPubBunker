<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const produtos = ref([])

const carregarProdutos = async () => {
  try {
    const response = await axios.get('http://localhost:8080/produtos')
    console.log("RESPOSTA:", response.data)
    produtos.value = response.data
  } catch (error) {
    console.error('Erro ao carregar produtos:', error)
  }
}

onMounted(() => {
  carregarProdutos()
})
</script>

<template>
  <div class="container">
    <h1>Cardápio - PubBunker</h1>

    <div v-if="produtos.length === 0">
      Nenhum produto encontrado.
    </div>

    <div v-else class="lista">
      <div class="card" v-for="p in produtos" :key="p.id">
        <h2>{{ p.nome }}</h2>
        <p>{{ p.descricao }}</p>
        <p><strong>Categoria:</strong> {{ p.categoria }}</p>
        <p class="preco">R$ {{ p.preco }}</p>
      </div>
    </div>
  </div>
</template>

<style>
body {
  margin: 0;
  background: #ffffff;
  color: #000000;
  font-family: Arial, sans-serif;
}

#app {
  min-height: 100vh;
  background: #ffffff;
  color: #000000;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

h1 {
  text-align: center;
  margin-bottom: 24px;
}

.lista {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.card {
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 16px;
  background: #f5f5f5;
}

.preco {
  font-weight: bold;
  color: green;
}
</style>