<script setup>
import logo from 'legacyAssets/logo.png'

const {
  nomeUsuario,
  role,
  logout
} = useAuth()
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

    <Button
        label="Logout"
        icon="pi pi-sign-out"
        severity="danger"
        @click="logout"
    />
  </header>
</template>