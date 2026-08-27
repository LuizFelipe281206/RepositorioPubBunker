<script setup>
import logo from 'legacyAssets/logo.png'

const {
  nomeUsuario,
  role,
  logout
} = useAuth()

const route = useRoute()

const irPara = async (caminho) => {
  await navigateTo(caminho)
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
          ({{ role }})
        </p>
      </div>
    </div>

    <div class="topo-acoes">
      <nav class="navegacao-topo">
        <template v-if="role === 'CLIENTE'">
          <Button
              label="Cardápio"
              icon="pi pi-book"
              severity="secondary"
              :outlined="!paginaAtual('/cardapio')"
              @click="irPara('/cardapio')"
          />

          <Button
              label="Avaliações"
              icon="pi pi-star"
              severity="secondary"
              :outlined="!paginaAtual('/avaliacoes')"
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
              severity="secondary"
              :outlined="!paginaAtual('/admin')"
              @click="irPara('/admin')"
          />

          <Button
              label="Reservas"
              icon="pi pi-calendar"
              severity="secondary"
              :outlined="!paginaAtual('/reservas')"
              @click="irPara('/reservas')"
          />
        </template>
      </nav>

      <Button
          label="Logout"
          icon="pi pi-sign-out"
          severity="danger"
          @click="logout"
      />
    </div>
  </header>
</template>