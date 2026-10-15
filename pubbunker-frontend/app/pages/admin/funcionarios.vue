<script setup>
definePageMeta({ role: 'ADMIN' })

const { $api } = useNuxtApp()
const { usuarioId, role } = useAuth()
const { abrirPopup } = usePopup()

const funcionarios = ref([])
const carregando = ref(false)
const processando = ref(false)
const erro = ref('')
const erroFormulario = ref('')
const modalVisivel = ref(false)
const modo = ref('criar')
const selecionado = ref(null)
const mostrarSenha = ref(false)
const campoEmail = ref(null)
const formulario = reactive({ nome: '', email: '', senha: '' })

const ocupado = computed(() => carregando.value || processando.value)

const tituloModal = computed(() => ({
  criar: 'Novo funcionário',
  editar: 'Editar funcionário',
  desativar: 'Desativar funcionário'
})[modo.value])

let paginaAtiva = true
let versao = 0

const podeConsultar = () =>
  paginaAtiva && role.value === 'ADMIN' && Boolean(usuarioId.value)

const normalizarEmail = email => {
  const valor = email.trim().toLowerCase()

  return valor && !valor.includes('@')
    ? valor + '@pubbunker.com'
    : valor
}

const dominioEmail = computed(() => {
  const emailAnterior = selecionado.value?.email || ''
  const inicioDominio = emailAnterior.lastIndexOf('@')

  return modo.value === 'editar' && inicioDominio >= 0
    ? emailAnterior.slice(inicioDominio)
    : '@pubbunker.com'
})

const usuarioEmail = computed({
  get: () => {
    const email = formulario.email

    return email.toLowerCase().endsWith(dominioEmail.value.toLowerCase())
      ? email.slice(0, -dominioEmail.value.length)
      : email
  },
  set: valor => {
    const texto = valor.trim()

    const usuario = texto.toLowerCase().endsWith(
      dominioEmail.value.toLowerCase()
    )
      ? texto.slice(0, -dominioEmail.value.length)
      : texto

    formulario.email = usuario ? usuario + dominioEmail.value : ''
  }
})

const focarEmail = () => {
  campoEmail.value?.focus()
}

const alternarSenha = () => {
  if (!processando.value) {
    mostrarSenha.value = !mostrarSenha.value
  }
}

const emailValido = email =>
  email.length <= 254 &&
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)

const senhaValida = senha =>
  Array.from(senha).length >= 8 &&
  senha.length <= 128 &&
  /[0-9]/.test(senha) &&
  /[\p{P}\p{S}]/u.test(senha)

const limparModal = () => {
  modalVisivel.value = false
  selecionado.value = null
  formulario.nome = ''
  formulario.email = ''
  formulario.senha = ''
  mostrarSenha.value = false
  erroFormulario.value = ''
}

const fecharModal = () => {
  if (!processando.value) limparModal()
}

const carregar = async () => {
  if (processando.value || modalVisivel.value) return

  const pedido = ++versao

  if (!podeConsultar()) {
    funcionarios.value = []
    carregando.value = false
    erro.value = 'Entre com uma conta de administrador para consultar.'
    return
  }

  const autor = usuarioId.value
  carregando.value = true
  erro.value = ''

  const respostaAtual = () =>
    pedido === versao &&
    autor === usuarioId.value &&
    podeConsultar()

  try {
    const { data } = await $api.get('/funcionarios')

    if (respostaAtual()) {
      funcionarios.value = data
    }
  } catch (falha) {
    if (!respostaAtual()) return

    funcionarios.value = []
    erro.value = falha.response?.data?.mensagem ||
      'Não foi possível carregar os funcionários.'
  } finally {
    if (pedido === versao) carregando.value = false
  }
}

const abrirModal = (acao, funcionario = null) => {
  if (
    !podeConsultar() ||
    ocupado.value ||
    modalVisivel.value ||
    !['criar', 'editar', 'desativar'].includes(acao)
  ) return

  if (
    acao !== 'criar' &&
    !funcionarios.value.some(item => item.id === funcionario?.id)
  ) return

  limparModal()
  modo.value = acao

  if (acao !== 'criar') {
    selecionado.value = { ...funcionario }
    formulario.nome = funcionario.nome || ''
    formulario.email = funcionario.email || ''
  }

  modalVisivel.value = true
}

const confirmar = async () => {
  if (
    !podeConsultar() ||
    ocupado.value ||
    !modalVisivel.value ||
    !['criar', 'editar', 'desativar'].includes(modo.value)
  ) return

  const acao = modo.value
  const id = selecionado.value?.id

  if (
    acao !== 'criar' &&
    !funcionarios.value.some(item => item.id === id)
  ) return

  erroFormulario.value = ''

  const dados = {
    nome: formulario.nome.trim(),
    email: normalizarEmail(formulario.email)
  }

  if (acao !== 'desativar') {
    if (!dados.nome || dados.nome.length > 100) {
      erroFormulario.value = 'Informe um nome com até 100 caracteres.'
      return
    }

    if (!emailValido(dados.email)) {
      erroFormulario.value = 'Informe um email válido com até 254 caracteres.'
      return
    }

    if (acao === 'criar') {
      const senha = formulario.senha

      if (!senhaValida(senha)) {
        erroFormulario.value =
          'A senha deve ter entre 8 e 128 caracteres, com pelo menos um número e um caractere especial.'
        return
      }

      dados.senha = senha
    }
  }

  const pedido = ++versao
  const autor = usuarioId.value
  processando.value = true

  const respostaAtual = () =>
    pedido === versao &&
    autor === usuarioId.value &&
    podeConsultar()

  try {
    if (acao === 'desativar') {
      await $api.delete(`/funcionarios/${encodeURIComponent(id)}`)

      if (!respostaAtual()) return

      funcionarios.value = funcionarios.value.filter(
        item => item.id !== id
      )
    } else {
      const { data } = acao === 'criar'
        ? await $api.post('/funcionarios', dados)
        : await $api.put(
          `/funcionarios/${encodeURIComponent(id)}`,
          dados
        )

      if (!respostaAtual()) return

      funcionarios.value = [
        ...funcionarios.value.filter(item => item.id !== data.id),
        data
      ].sort((a, b) =>
        String(a.nome || '').localeCompare(
          String(b.nome || ''),
          'pt-BR'
        )
      )
    }

    limparModal()

    abrirPopup('Sucesso', {
      criar: 'Funcionário cadastrado.',
      editar: 'Funcionário atualizado.',
      desativar: 'Funcionário desativado.'
    }[acao])
  } catch (falha) {
    if (!respostaAtual()) return

    const resposta = falha.response?.data

    erroFormulario.value =
      Object.values(resposta?.campos || {}).join(' ') ||
      resposta?.mensagem ||
      'Não foi possível concluir a operação.'

    if (falha.response?.status === 429) {
      abrirPopup('Aguarde', erroFormulario.value, 'erro', 60)
    }
  } finally {
    if (pedido === versao) processando.value = false
  }
}

watch([usuarioId, role], () => {
  versao++
  funcionarios.value = []
  carregando.value = false
  processando.value = false
  erro.value = ''
  limparModal()

  if (podeConsultar()) carregar()
})

onBeforeUnmount(() => {
  paginaAtiva = false
  versao++
  funcionarios.value = []
  limparModal()
})

onMounted(carregar)
</script>

<template>
  <main class="container">
    <AppHeader />

    <div class="cabecalho">
      <div>
        <h2>Funcionários</h2>
        <p class="texto-secundario">
          Ativos: {{ funcionarios.length }}
        </p>
      </div>

      <div class="acoes">
        <Button
          label="Novo funcionário"
          icon="pi pi-user-plus"
          :disabled="ocupado || modalVisivel"
          @click="abrirModal('criar')"
        />

        <Button
          label="Atualizar"
          icon="pi pi-refresh"
          severity="secondary"
          :loading="carregando"
          :disabled="ocupado || modalVisivel"
          @click="carregar"
        />
      </div>
    </div>

    <p v-if="carregando" class="texto-secundario">
      Carregando funcionários...
    </p>

    <p v-else-if="erro" class="painel" role="alert">
      {{ erro }}
    </p>

    <p v-else-if="!funcionarios.length" class="texto-secundario">
      Nenhum funcionário ativo cadastrado.
    </p>

    <div v-else class="lista-funcionarios">
      <Card
        v-for="funcionario in funcionarios"
        :key="funcionario.id"
      >
        <template #title>
          {{ funcionario.nome }}
        </template>

        <template #content>
          <p class="email">{{ funcionario.email }}</p>
        </template>

        <template #footer>
          <div class="acoes">
            <Button
              label="Editar"
              icon="pi pi-pencil"
              severity="secondary"
              :aria-label="'Editar ' + funcionario.nome"
              :disabled="ocupado || modalVisivel"
              @click="abrirModal('editar', funcionario)"
            />

            <Button
              label="Desativar"
              icon="pi pi-user-minus"
              severity="danger"
              :aria-label="'Desativar ' + funcionario.nome"
              :disabled="ocupado || modalVisivel"
              @click="abrirModal('desativar', funcionario)"
            />
          </div>
        </template>
      </Card>
    </div>

    <Dialog
      v-model:visible="modalVisivel"
      :header="tituloModal"
      modal
      :closable="!processando"
      :close-on-escape="!processando"
      :style="{ width: 'min(480px, 92vw)' }"
      @hide="fecharModal"
    >
      <p v-if="modo === 'desativar'">
        Desativar a conta de
        <strong>{{ selecionado?.nome }}</strong>?
        Esta conta perderá o acesso ao sistema.
      </p>

      <form
        v-else
        id="form-funcionario"
        class="formulario"
        @submit.prevent="confirmar"
      >
        <div class="campo">
          <label for="funcionario-nome">Nome</label>

          <InputText
            id="funcionario-nome"
            v-model="formulario.nome"
            maxlength="100"
            autocomplete="off"
            required
            :disabled="processando"
          />
        </div>

        <div class="campo">
          <label for="funcionario-email">Email</label>

          <div
            class="email-fixo"
            :class="{ 'email-desabilitado': processando }"
            @click="focarEmail"
          >
            <span class="email-usuario">
              <span class="email-medida" aria-hidden="true">
                {{ usuarioEmail }}
              </span>

              <input
                id="funcionario-email"
                ref="campoEmail"
                v-model="usuarioEmail"
                type="text"
                inputmode="email"
                size="1"
                :maxlength="254 - dominioEmail.length"
                autocomplete="off"
                autocapitalize="none"
                :spellcheck="false"
                aria-describedby="funcionario-email-dominio"
                required
                :disabled="processando"
              />
            </span>

            <span
              id="funcionario-email-dominio"
              class="email-dominio"
            >{{ dominioEmail }}</span>
          </div>
        </div>

        <div v-if="modo === 'criar'" class="campo">
          <label for="funcionario-senha">Senha</label>

          <div class="entrada-senha">
            <InputText
              id="funcionario-senha"
              v-model="formulario.senha"
              :type="mostrarSenha ? 'text' : 'password'"
              minlength="8"
              maxlength="128"
              autocomplete="new-password"
              required
              :disabled="processando"
            />

            <button
              type="button"
              class="botao-olho"
              :aria-label="mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'"
              :aria-pressed="mostrarSenha"
              aria-controls="funcionario-senha"
              :disabled="processando"
              @click="alternarSenha"
            >
              <i
                :class="mostrarSenha ? 'pi pi-eye-slash' : 'pi pi-eye'"
                aria-hidden="true"
              />
            </button>
          </div>

          <small class="texto-secundario">
            De 8 a 128 caracteres, incluindo um número
            e um caractere especial.
          </small>
        </div>
      </form>

      <p
        v-if="erroFormulario"
        role="alert"
        class="erro-formulario"
      >
        {{ erroFormulario }}
      </p>

      <template #footer>
        <Button
          label="Cancelar"
          severity="secondary"
          :disabled="processando"
          @click="fecharModal"
        />

        <Button
          v-if="modo === 'desativar'"
          label="Desativar"
          severity="danger"
          :loading="processando"
          :disabled="processando"
          @click="confirmar"
        />

        <Button
          v-else
          :label="modo === 'criar' ? 'Cadastrar' : 'Salvar'"
          type="submit"
          form="form-funcionario"
          :loading="processando"
          :disabled="processando"
        />
      </template>
    </Dialog>
  </main>
</template>

<style scoped>
.cabecalho {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin: 24px 0;
}

.acoes {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.lista-funcionarios {
  display: grid;
  grid-template-columns: repeat(
    auto-fit,
    minmax(min(100%, 280px), 1fr)
  );
  gap: 16px;
}

.formulario {
  display: grid;
  gap: 16px;
}

.campo {
  display: grid;
  gap: 6px;
}

.email-fixo {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  min-height: 44px;
  padding:
    var(--p-inputtext-padding-y, 0.75rem)
    var(--p-inputtext-padding-x, 0.75rem);
  border: 1px solid var(--p-inputtext-border-color, #cbd5e1);
  border-radius: var(--p-inputtext-border-radius, 6px);
  background: var(--p-inputtext-background, #fff);
  color: var(--p-inputtext-color, #334155);
  font: inherit;
  cursor: text;
}

.email-fixo:focus-within {
  border-color: var(--p-inputtext-focus-border-color, #9e3036);
  outline: 2px solid var(--p-inputtext-focus-border-color, #9e3036);
  outline-offset: 2px;
}

.email-usuario {
  display: inline-grid;
  grid-template-columns: minmax(0, 1fr);
  flex: 0 1 auto;
  min-width: 1px;
}

.email-medida,
.email-usuario input {
  grid-area: 1 / 1;
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font: inherit;
  letter-spacing: inherit;
}

.email-medida {
  visibility: hidden;
  white-space: pre;
  padding-right: 2px;
}

.email-usuario input {
  width: 100%;
  min-width: 0;
  border: 0;
  border-radius: 0;
  outline: none;
  background: transparent;
  color: inherit;
  box-shadow: none;
}

.email-dominio {
  flex: 0 0 auto;
  color: var(--p-inputtext-placeholder-color, #64748b);
  white-space: nowrap;
}

.email-desabilitado {
  background: var(--p-inputtext-disabled-background, #e2e8f0);
  color: var(--p-inputtext-disabled-color, #64748b);
  cursor: wait;
}

.entrada-senha {
  position: relative;
}

.entrada-senha :deep(.p-inputtext) {
  box-sizing: border-box;
  width: 100%;
  min-height: 44px;
  padding-right: 50px;
}

.botao-olho {
  position: absolute;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #655347;
  cursor: pointer;
}

.botao-olho:focus-visible {
  outline: 2px solid #9e3036;
  outline-offset: -4px;
}

.botao-olho:disabled {
  cursor: wait;
  opacity: 0.6;
}

.email {
  overflow-wrap: anywhere;
}

.erro-formulario {
  color: #9e3036;
  margin-top: 16px;
}
</style>