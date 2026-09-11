<script setup>
definePageMeta({
  public: true
})

const route = useRoute()
const { acessarComanda, erroComanda } = useComanda()

const carregando = ref(true)
const erroAcesso = ref(false)
const mensagemErro = ref('')
const tituloErro = ref('Comanda indisponível')

const acessar = async () => {
  carregando.value = true
  erroAcesso.value = false

  try {
    const codigo = Array.isArray(route.params.codigo)
        ? route.params.codigo[0]
        : route.params.codigo

    if (!codigo) {
      throw new Error('Código da comanda não informado.')
    }

    const acessoPermitido = await acessarComanda(codigo)

    if (acessoPermitido) {
      window.location.replace('/cardapio')
    } else {
      erroAcesso.value = true
      tituloErro.value = erroComanda.value.titulo
      mensagemErro.value = erroComanda.value.mensagem
    }
  } catch (erro) {
    erroAcesso.value = true
    tituloErro.value = 'Não foi possível acessar a comanda'
    mensagemErro.value =
        erro?.data?.mensagem ||
        erro?.message ||
        'Não foi possível acessar esta comanda.'
  } finally {
    carregando.value = false
  }
}

onMounted(acessar)
</script>

<template>
  <main class="pagina-centralizada">
    <Card class="login-card">
      <template #content>
        <div v-if="carregando">
          Acessando comanda...
        </div>

        <div v-else-if="erroAcesso" class="erro-acesso">
          <h2>{{ tituloErro }}</h2>

          <p>{{ mensagemErro }}</p>
          <Button
    label="Tentar novamente"
    icon="pi pi-refresh"
    @click="acessar"
/>
          <Button
              label="Voltar"
              icon="pi pi-arrow-left"
              @click="navigateTo('/login')"
          />
        </div>
      </template>
    </Card>
  </main>
</template>

<style scoped>
.pagina-centralizada {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 20px;
  background: #f3f3f3;
}

.login-card {
  width: min(420px, 100%);
  text-align: center;
}

.erro-acesso {
  display: grid;
  gap: 16px;
}

.erro-acesso h2,
.erro-acesso p {
  margin: 0;
}
</style>