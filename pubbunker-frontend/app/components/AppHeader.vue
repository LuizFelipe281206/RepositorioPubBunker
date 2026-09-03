<script setup>
import logo from 'legacyAssets/logo.png'

const {
  nomeUsuario: nomeUsuarioConta,
  role: roleConta,
  logout
} = useAuth()

const {
  comandaAtual,
  comandaAtiva,
  sairComanda
} = useComanda()

const route = useRoute()

const nomeUsuario = computed(() => {
  if (comandaAtiva.value) {
    return `Comanda ${comandaAtual.value.numero}`
  }

  return nomeUsuarioConta.value
})

const role = computed(() => {
  if (comandaAtiva.value) {
    return 'CLIENTE'
  }

  return roleConta.value
})

const irPara = async (caminho) => {
  await navigateTo(caminho)
}

const sair = async () => {
  if (comandaAtiva.value) {
    await sairComanda()
    return
  }

  await logout()
}

const paginaAtual = (caminho) =>
    route.path === caminho
</script>

<template>
  <header class="topo">
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
  <strong>{{ nomeUsuario }}</strong>
</p>
      </div>
    </div>

    <div class="topo-acoes">
      <nav class="navegacao-topo">
  <template v-if="role === 'CLIENTE'">
    <Button
        label="Cardápio"
        icon="pi pi-book"
        class="botao-navegacao"
        :class="{
          'botao-navegacao-ativo':
              paginaAtual('/cardapio')
        }"
        @click="irPara('/cardapio')"
    />
    <Button
    label="Pedidos"
    icon="pi pi-shopping-cart"
    class="botao-navegacao"
    :class="{
      'botao-navegacao-ativo':
        paginaAtual('/pedidos')
    }"
    @click="irPara('/pedidos')"
    />
    <Button
        label="Avaliações"
        icon="pi pi-star"
        class="botao-navegacao"
        :class="{
          'botao-navegacao-ativo':
              paginaAtual('/avaliacoes')
        }"
        @click="irPara('/avaliacoes')"
    />
  </template>

  <template
      v-if="
        role === 'ADMIN' ||
        role === 'FUNCIONARIO'
      "
  >
    <Button
        v-if="role === 'ADMIN'"
        label="Painel"
        icon="pi pi-home"
        class="botao-navegacao"
        :class="{
          'botao-navegacao-ativo':
              paginaAtual('/admin')
        }"
        @click="irPara('/admin')"
    />
        <Button
    label="Comandas"
    icon="pi pi-qrcode"
    class="botao-navegacao"
    :class="{
      'botao-navegacao-ativo':
          paginaAtual('/admin/comandas')
    }"
    @click="irPara('/admin/comandas')"
/>
    <Button
        label="Reservas"
        icon="pi pi-calendar"
        class="botao-navegacao"
        :class="{
          'botao-navegacao-ativo':
              paginaAtual('/reservas')
        }"
        @click="irPara('/reservas')"
    />
  </template>
</nav>

      <Button
          label="Logout"
          icon="pi pi-sign-out"
          severity="danger"
          @click="sair"
      />
    </div>
  </header>
</template>