<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

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

      <h1>Login</h1>

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
          v-for="produto in carrinho"
          :key="produto.id"
        >

          {{ produto.nome }}
          - R$ {{ produto.preco }}

        </div>

        <button
          class="btn-finalizar"
          @click="finalizarPedido"
        >
          Finalizar Pedido
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
  background: #f4f4f4;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.login-box {
  max-width: 400px;
  margin: 80px auto;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.login-box input {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  border-radius: 8px;
  border: 1px solid #ccc;
}

.topo {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-logout {
  background: #444;
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

.card {
  background: white;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
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

.btn-pedido {
  background: #5cb85c;
  color: white;
}

.carrinho {
  margin-top: 24px;
  background: white;
  padding: 20px;
  border-radius: 12px;
}

.btn-finalizar {
  margin-top: 12px;
  background: #0275d8;
  color: white;
}
</style>