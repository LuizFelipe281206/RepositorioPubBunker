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
    <Card class="login-card">
      <template #header>
        <img
            :src="logo"
            alt="PubBunker"
            class="logo-login"
        >
      </template>

      <template #title>
        PubBunker
      </template>

      <template #content>
        <form
            class="form-login"
            @submit.prevent="entrar"
        >
          <div class="campo">
            <label for="email">
              E-mail
            </label>

            <InputText
                id="email"
                v-model="formulario.email"
                type="email"
                autocomplete="username"
                fluid
            />
          </div>

          <div class="campo">
            <label for="senha">
              Senha
            </label>

            <Password
                id="senha"
                v-model="formulario.senha"
                :feedback="false"
                toggle-mask
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
              fluid
          />
        </form>
      </template>
    </Card>
  </main>
</template>