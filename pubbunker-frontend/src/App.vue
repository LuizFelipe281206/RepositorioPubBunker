<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import logo from './assets/logo.png'

const API_URL = 'http://localhost:8080/produtos'
const AUTH_URL = 'http://localhost:8080/auth/login'

const produtos = ref([])
const carrinho = ref([])

const editando = ref(false)
const idEditando = ref(null)

const usuarioLogado = ref(false)
const role = ref('')
const nomeUsuario = ref('')

const loginForm = ref({
  email: '',
  senha: ''
})

const form = ref({
  nome: '',
  descricao: '',
  preco: '',
  categoria: '',
  ativo: true
})

const adicionarAoPedido = (produto) => {

  carrinho.value.push(produto)
}

const removerDoPedido = (index) => {

  carrinho.value.splice(index, 1)
}

const cancelarPedido = () => {

  carrinho.value = []
}

const carregarProdutos = async () => {

  try {

    const response = await axios.get(API_URL)

    produtos.value = response.data

  } catch (error) {

    console.error('Erro ao carregar produtos:', error)
  }
}

const login = async () => {

  try {

    const response = await axios.post(
      AUTH_URL,
      {
        email: loginForm.value.email,
        senha: loginForm.value.senha
      }
    )

    localStorage.setItem(
      'role',
      response.data.role
    )

    localStorage.setItem(
      'nome',
      response.data.nome
    )

    role.value = response.data.role
    nomeUsuario.value = response.data.nome
    usuarioLogado.value = true

    alert('Login realizado com sucesso!')

  } catch (error) {

    alert('Email ou senha inválidos')
  }
}

const logout = () => {

  localStorage.removeItem('role')
  localStorage.removeItem('nome')

  usuarioLogado.value = false
  role.value = ''
  nomeUsuario.value = ''
}

const verificarLogin = () => {

  const roleStorage =
    localStorage.getItem('role')

  const nomeStorage =
    localStorage.getItem('nome')

  if(roleStorage) {

    usuarioLogado.value = true

    role.value = roleStorage

    nomeUsuario.value = nomeStorage
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

  if(role.value !== 'ADMIN') {

    alert(
      'Apenas administradores podem cadastrar produtos'
    )

    return
  }

  try {

    const payload = {
      nome: form.value.nome,
      descricao: form.value.descricao,
      preco: Number(form.value.preco),
      categoria: form.value.categoria,
      ativo: form.value.ativo
    }

    if(editando.value) {

      await axios.put(
        `${API_URL}/${idEditando.value}`,
        payload
      )

    } else {

      await axios.post(
        API_URL,
        payload
      )
    }

    await carregarProdutos()

    limparFormulario()

  } catch (error) {

    console.error(
      'Erro ao salvar produto:',
      error
    )
  }
}

const editarProduto = (produto) => {

  if(role.value !== 'ADMIN') {

    alert(
      'Apenas administradores podem editar produtos'
    )

    return
  }

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

  if(role.value !== 'ADMIN') {

    alert(
      'Apenas administradores podem excluir produtos'
    )

    return
  }

  try {

    await axios.delete(
      `${API_URL}/${id}`
    )

    await carregarProdutos()

  } catch (error) {

    console.error(
      'Erro ao excluir produto:',
      error
    )
  }
}

const adicionarCarrinho = (produto) => {

  carrinho.value.push(produto)

  alert('Produto adicionado!')
}

const finalizarPedido = async () => {

  try {

    const produtosIds =
      carrinho.value.map(
        produto => produto.id
      )

    await axios.post(
      'http://localhost:8080/pedidos',
      {
        clienteId: 3,
        produtosIds: produtosIds
      }
    )

    alert('Pedido realizado!')

    carrinho.value = []

  } catch(error) {

    console.error(error)

    alert('Erro ao finalizar pedido')
  }
}

onMounted(() => {

  verificarLogin()

  carregarProdutos()
})
</script>

<template>

  <div class="container">

    <div
        v-if="!usuarioLogado"
        class="login-box"
    >

      <img
          :src="logo"
          alt="PubBunker"
          class="logo-login"
      />

      <h1>PubBunker</h1>

      <input
          v-model="loginForm.email"
          type="text"
          placeholder="Email"
      />

      <input
          v-model="loginForm.senha"
          type="password"
          placeholder="Senha"
      />

      <button @click="login">
        Entrar
      </button>

    </div>

<div v-else>

  <div class="topo">

    <div class="logo-area">

      <img
          :src="logo"
          alt="PubBunker"
          class="logo"
      >

      <div>

        <h1>PubBunker</h1>

        <p>
          Bem-vindo,
          <strong>
            {{ nomeUsuario }}
          </strong>

          ({{ role }})
        </p>

      </div>

    </div>

    <button
        class="btn-logout"
        @click="logout"
    >
      Logout
    </button>

  </div>

  <div
    v-if="role === 'ADMIN'"
    class="formulario"
  >

    <h2>
      {{
        editando
        ? 'Editar Produto'
        : 'Cadastrar Produto'
      }}
    </h2>

    <input
      v-model="form.nome"
      type="text"
      placeholder="Nome do produto"
    />

    <input
      v-model="form.descricao"
      type="text"
      placeholder="Descrição"
    />

    <input
      v-model="form.preco"
      type="number"
      step="0.01"
      placeholder="Preço"
    />

    <input
      v-model="form.categoria"
      type="text"
      placeholder="Categoria"
    />

    <label class="checkbox">

      <input
        v-model="form.ativo"
        type="checkbox"
      />

      Ativo

    </label>

    <div class="acoes-form">

      <button @click="salvarProduto">

        {{
          editando
          ? 'Atualizar Produto'
          : 'Cadastrar Produto'
        }}

      </button>

      <button
        v-if="editando"
        class="btn-cancelar"
        @click="limparFormulario"
      >
        Cancelar
      </button>

    </div>

  </div>

  <div class="lista">

    <div
      class="card"
      v-for="produto in produtos"
      :key="produto.id"
    >

      <h2>{{ produto.nome }}</h2>

      <p>
        <strong>Descrição:</strong>
        {{ produto.descricao }}
      </p>

      <p>
        <strong>Preço:</strong>
        R$ {{ produto.preco }}
      </p>

      <p>
        <strong>Categoria:</strong>
        {{ produto.categoria }}
      </p>

      <p>
        <strong>Ativo:</strong>
        {{ produto.ativo ? 'Sim' : 'Não' }}
      </p>

      <div class="acoes-card">

        <button
          v-if="role === 'ADMIN'"
          class="btn-editar"
          @click="editarProduto(produto)"
        >
          Editar
        </button>

        <button
          v-if="role === 'ADMIN'"
          class="btn-excluir"
          @click="excluirProduto(produto.id)"
        >
          Excluir
        </button>

        <button
          v-if="role === 'CLIENTE'"
          class="btn-pedido"
          @click="adicionarCarrinho(produto)"
        >
          Adicionar ao Pedido
        </button>

      </div>

    </div>

  </div>

  <div
    v-if="role === 'CLIENTE'"
    class="carrinho"
  >

    <h2>Seu Pedido</h2>

    <div
      v-for="(produto, index) in carrinho"
      :key="index"
      class="item-carrinho"
    >

      <span>
        {{ produto.nome }}
        - R$ {{ produto.preco }}
      </span>

      <button
        class="btn-remover"
        @click="removerDoPedido(index)"
      >
        Remover
      </button>

    </div>

    <button
      class="btn-finalizar"
      @click="finalizarPedido"
    >
      Finalizar Pedido
    </button>

    <button
      class="btn-cancelar-pedido"
      @click="cancelarPedido"
    >
      Cancelar Pedido
    </button>

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
  background:#ececec;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.login-box {
  max-width: 420px;
  margin: 90px auto;
  background: #1f1f1f;
  color: white;
  padding: 32px;
  border-radius: 18px;
  box-shadow: 0 6px 18px rgba(0,0,0,.18);
  text-align: center;
}

.login-box h1 {
  margin-top: 4px;
  margin-bottom: 24px;
  font-size: 32px;
}

.login-box input {
  width: 100%;
  padding: 12px;
  margin-bottom: 14px;
  border-radius: 10px;
  border: 1px solid #333;
  font-size: 15px;
}

.login-box input:focus {
  outline: none;
  border-color: #8b0000;
  box-shadow: 0 0 0 2px rgba(139,0,0,.25);
}

.login-box button {
  width: 100%;
  margin-top: 8px;
  background: #8b0000;
  color: white;
  padding: 12px;
}

.login-box input::placeholder {
  color: #888;
}

.login-box button:hover {
  background: #9f0000;
  transform: translateY(-2px);
}

.topo{
  display:flex;
  justify-content:space-between;
  align-items:center;

  background:#1f1f1f;
  color:white;

  padding:20px;
  border-radius:14px;

  margin-bottom:24px;
}

.btn-logout{
  background:#8b0000;
  color: white;
}

.formulario {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
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
  grid-template-columns: repeat(
    auto-fit,
    minmax(260px, 1fr)
  );
  gap: 16px;
}

.card{
  background:white;
  padding:18px;
  border-radius:14px;

  box-shadow:
      0 4px 12px rgba(0,0,0,.08);

  transition:.2s;
}

.card:hover{
  transform:translateY(-4px);
}

.card h2 {
  margin-top: 0;
}

.acoes-card {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.btn-editar{
  background:#f28c28;
  color:white;
}

.btn-excluir{
  background:#8b0000;
  color:white;
}

.btn-pedido{
  background:#5f6f1f;
  color:white;
}

.carrinho{
  margin-top:24px;
  background:white;
  padding:20px;
  border-radius:12px;

  box-shadow:
      0 4px 12px rgba(0,0,0,.08);
}

.btn-finalizar{
  background:#5f6f1f;
  color:white;
}

.item-carrinho {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.btn-remover {
  background: #d9534f;
  color: white;
}

.btn-cancelar-pedido {
  margin-left: 10px;
  background: #777;
  color: white;
}

.logo-area{
  display:flex;
  align-items:center;
  gap:16px;
}

.logo{
  width:80px;
  height:80px;
  object-fit:contain;
}

.logo-login {
  width: 140px;
  height: 140px;
  object-fit: contain;
  margin-bottom: 12px;
}
</style>