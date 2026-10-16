<script setup>
definePageMeta({ public: true })

const route = useRoute()
const { acessarComanda, cancelarAcessoComanda, erroComanda, exigeCodigoComanda } = useComanda()

const carregando = ref(false)
const erroAcesso = ref(false)
const mensagemErro = ref('')
const tituloErro = ref('Comanda indisponível')
const codigoDigitado = ref('')
let versaoPagina = 0
let paginaAtiva = true

const codigoDaRota = () => Array.isArray(route.params.codigo)
    ? route.params.codigo[0]
    : route.params.codigo

const acessar = async (codigoEntrada = null) => {
  if (carregando.value || !paginaAtiva) return

  const versao = ++versaoPagina
  const codigo = codigoDaRota()
  carregando.value = true
  erroAcesso.value = false

  try {
    if (!codigo) throw new Error('Código da comanda não informado.')

    const permitido = await acessarComanda(codigo, codigoEntrada)
    if (!paginaAtiva || versao !== versaoPagina || codigo !== codigoDaRota()) return

    if (permitido) {
      await navigateTo('/cardapio', { replace: true })
    } else {
      erroAcesso.value = true
      tituloErro.value = erroComanda.value?.titulo || 'Não foi possível entrar'
      mensagemErro.value = erroComanda.value?.mensagem || 'Tente novamente em instantes.'
    }
  } catch (erro) {
    if (!paginaAtiva || versao !== versaoPagina) return
    erroAcesso.value = true
    tituloErro.value = 'Não foi possível acessar a comanda'
    mensagemErro.value = erro?.response?.data?.mensagem || erro?.message ||
        'Não foi possível acessar esta comanda.'
  } finally {
    if (versao === versaoPagina) carregando.value = false
  }
}

const filtrarCodigo = evento => {
  codigoDigitado.value = evento.target.value.replace(/\D/g, '').slice(0, 5)
  evento.target.value = codigoDigitado.value
}

const confirmarCodigo = () => {
  if (/^[0-9]{5}$/.test(codigoDigitado.value)) acessar(codigoDigitado.value)
}

onMounted(() => acessar())

watch(() => route.params.codigo, () => {
  versaoPagina++
  carregando.value = false
  codigoDigitado.value = ''
  acessar()
})

onBeforeUnmount(() => {
  paginaAtiva = false
  versaoPagina++
  cancelarAcessoComanda()
})
</script>

<template>
  <main class="pagina-centralizada">
    <Card class="login-card">
      <template #content>
        <div v-if="exigeCodigoComanda" class="entrada-comanda">
          <h2>Entre na comanda</h2>
          <p>Peça à equipe o código de cinco dígitos deste atendimento.</p>

          <form class="form-codigo" @submit.prevent="confirmarCodigo">
            <label for="codigo-entrada">Código da comanda</label>
            <InputText
                id="codigo-entrada"
                v-model="codigoDigitado"
                inputmode="numeric"
                autocomplete="off"
                pattern="[0-9]{5}"
                minlength="5"
                maxlength="5"
                placeholder="00000"
                class="campo-codigo"
                :disabled="carregando"
                :aria-invalid="erroAcesso && !carregando"
                aria-describedby="erro-codigo"
                required
                fluid
                @input="filtrarCodigo"
            />
            <p id="erro-codigo" class="mensagem-codigo" role="status" aria-live="polite">
              {{ erroAcesso ? mensagemErro : '' }}
            </p>
            <Button
                type="submit"
                label="Entrar"
                icon="pi pi-sign-in"
                :loading="carregando"
                :disabled="carregando || !/^[0-9]{5}$/.test(codigoDigitado)"
            />
          </form>
          <Button label="Voltar" severity="secondary" text @click="navigateTo('/login')" />
        </div>

        <div v-else-if="carregando" role="status">Acessando comanda...</div>

        <div v-else-if="erroAcesso" class="erro-acesso">
          <h2>{{ tituloErro }}</h2>
          <p>{{ mensagemErro }}</p>
          <Button label="Tentar novamente" icon="pi pi-refresh" @click="acessar()" />
          <Button label="Voltar" icon="pi pi-arrow-left" @click="navigateTo('/login')" />
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
.login-card { width: min(420px, 100%); text-align: center; }
.erro-acesso, .entrada-comanda, .form-codigo { display: grid; gap: 16px; }
h2, p { margin: 0; }
.form-codigo { text-align: left; }
.campo-codigo { text-align: center; font-size: 1.6rem; letter-spacing: .35rem; }
.mensagem-codigo { color: #8b252c; font-size: .9rem; min-height: 1.2em; }
</style>
