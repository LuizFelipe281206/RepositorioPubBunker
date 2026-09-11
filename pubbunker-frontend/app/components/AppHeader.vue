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
      <nav
        class="navegacao-topo"
        aria-label="Navegação principal"
      >
        <template v-if="role === 'CLIENTE'">
          <Button
            label="Cardápio"
            icon="pi pi-book"
            class="botao-navegacao"
            :class="{
              'botao-navegacao-ativo': paginaAtual('/cardapio')
            }"
            @click="irPara('/cardapio')"
          />

          <Button
            label="Pedidos"
            icon="pi pi-shopping-cart"
            class="botao-navegacao"
            :class="{
              'botao-navegacao-ativo': paginaAtual('/pedidos')
            }"
            @click="irPara('/pedidos')"
          />

          <Button
            label="Avaliações"
            icon="pi pi-star"
            class="botao-navegacao"
            :class="{
              'botao-navegacao-ativo': paginaAtual('/avaliacoes')
            }"
            @click="irPara('/avaliacoes')"
          />
        </template>

        <template
          v-if="role === 'ADMIN' || role === 'FUNCIONARIO'"
        >
          <Button
            v-if="role === 'ADMIN'"
            label="Painel"
            icon="pi pi-home"
            class="botao-navegacao"
            :class="{
              'botao-navegacao-ativo': paginaAtual('/admin')
            }"
            @click="irPara('/admin')"
          />

          <Button
            label="Comandas"
            icon="pi pi-qrcode"
            class="botao-navegacao"
            :class="{
              'botao-navegacao-ativo': paginaAtual('/admin/comandas')
            }"
            @click="irPara('/admin/comandas')"
          />

          <Button
            label="Pedidos"
            icon="pi pi-shopping-cart"
            class="botao-navegacao"
            :class="{
              'botao-navegacao-ativo': paginaAtual('/admin/pedidos')
            }"
            @click="irPara('/admin/pedidos')"
          />

          <Button
            label="Reservas"
            icon="pi pi-calendar"
            class="botao-navegacao"
            :class="{
              'botao-navegacao-ativo': paginaAtual('/reservas')
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

<style scoped>
.topo {
  flex-wrap: wrap;
}

.topo .navegacao-topo {
  background: transparent;
  border: none;
  padding: 0;
  gap: 8px;
}

.topo :deep(.botao-navegacao.p-button) {
  width: auto;
  min-height: 44px;
  padding: 0.65rem 0.85rem;
  white-space: nowrap;
  background: transparent !important;
  color: #f5eee3 !important;
  border: 1px solid #655347;
  border-radius: 8px;
  box-shadow: none;
}

.topo :deep(.botao-navegacao.p-button:not(:disabled):hover) {
  background: #3b3029 !important;
  border-color: #cdbda6;
}

.topo :deep(.botao-navegacao-ativo.p-button) {
  background: var(--bunker-wine) !important;
  border-color: #b85c63;
  color: white !important;
}

.topo :deep(.p-button:focus-visible) {
  outline: 2px solid #e4bd7b;
  outline-offset: 3px;
}

.topo-acoes > :deep(.p-button-danger) {
  width: auto;
  min-height: 44px;
  white-space: nowrap;
}

@media (max-width: 640px) {
  .topo .navegacao-topo {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .topo .topo-acoes :deep(.botao-navegacao.p-button) {
    width: 100%;
    min-width: 0;
    min-height: 44px;
    padding: 0.6rem 0.35rem;
    font-size: 0.8rem;
  }

  .topo-acoes > :deep(.p-button-danger) {
    width: 38px;
    min-height: 38px;
  }
}
</style>