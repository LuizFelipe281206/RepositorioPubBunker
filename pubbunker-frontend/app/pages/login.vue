<script setup>
import logo from 'legacyAssets/logo.png'

definePageMeta({
  public: true
})

const { login } = useAuth()

const formulario = reactive({
  email: '',
  senha: ''
})

const enviando = ref(false)

const entrar = async () => {
  enviando.value = true

  try {
    await login(formulario)
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <main class="pagina-login">
    <section class="login-mobile">
      <header class="login-marca">
        <img
            :src="logo"
            alt="Logo do PubBunker"
            class="logo-login"
        >

        <span class="login-selo">
          Gastro Pub
        </span>

        <h1>PubBunker</h1>
      </header>

      <div class="login-formulario">
        <header class="login-cabecalho">
          <h2>Bem-vindo</h2>

          <p>
            Entre com seus dados para continuar.
          </p>
        </header>

        <form
            class="form-login"
            @submit.prevent="entrar"
        >
          <div class="campo">
            <label for="email">
              E-mail
              <span class="campo-obrigatorio">*</span>
            </label>

            <InputText
                id="email"
                v-model="formulario.email"
                type="email"
                autocomplete="username"
                required
                fluid
            />
          </div>

          <div class="campo">
            <label for="senha">
              Senha
              <span class="campo-obrigatorio">*</span>
            </label>

            <Password
                id="senha"
                v-model="formulario.senha"
                :feedback="false"
                toggle-mask
                required
                fluid
                input-class="campo-senha"
                autocomplete="current-password"
            />
          </div>

          <Button
              type="submit"
              label="Entrar"
              icon="pi pi-sign-in"
              :loading="enviando"
              class="botao-login"
              fluid
          />
        </form>
      </div>
    </section>
  </main>
</template>