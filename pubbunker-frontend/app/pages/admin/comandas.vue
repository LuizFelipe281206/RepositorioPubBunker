<script setup>
import QRCode from 'qrcode'

definePageMeta({
  roles: ['ADMIN', 'FUNCIONARIO']
})

const {
  comandas,
  carregandoComandas,
  carregarComandas,
  criarComanda,
  abrirComanda,
  fecharComanda,
  arquivarComanda
} = useComandas()

const { abrirPopup } = usePopup()

const confirmacaoArquivamentoVisivel = ref(false)
const comandaParaArquivar = ref(null)

const numeroNovaComanda = ref(null)
const criandoComanda = ref(false)
const processandoId = ref(null)

const qrCodeVisivel = ref(false)
const qrCodeImagem = ref('')
const comandaSelecionada = ref(null)
const gerandoQrCode = ref(false)

const montarLinkComanda = (codigoAcesso) => {
  if (!import.meta.client || !codigoAcesso) {
    return ''
  }

  return `${window.location.origin}/comanda/${codigoAcesso}`
}

const mostrarQrCode = async (comanda) => {
  comandaSelecionada.value = comanda
  qrCodeVisivel.value = true
  qrCodeImagem.value = ''
  gerandoQrCode.value = true

  try {
    const link = montarLinkComanda(
        comanda.codigoAcesso
    )

    qrCodeImagem.value = await QRCode.toDataURL(
        link,
        {
          width: 320,
          margin: 2,
          color: {
            dark: '#1e1c19',
            light: '#fffdf9'
          }
        }
    )
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível gerar o QR Code.',
        'erro'
    )

    qrCodeVisivel.value = false
  } finally {
    gerandoQrCode.value = false
  }
}

const copiarLinkQrCode = async () => {
  const link = montarLinkComanda(
      comandaSelecionada.value?.codigoAcesso
  )

  if (!link) {
    return
  }

  try {
    await navigator.clipboard.writeText(link)

    abrirPopup(
        'Link copiado',
        'O link da comanda foi copiado.'
    )
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível copiar o link.',
        'erro'
    )
  }
}

onMounted(carregarComandas)

const criar = async () => {
  if (!numeroNovaComanda.value) {
    return
  }

  criandoComanda.value = true

  try {
    const comanda = await criarComanda(
        numeroNovaComanda.value
    )

    if (comanda) {
      numeroNovaComanda.value = null
    }
  } finally {
    criandoComanda.value = false
  }
}

const abrir = async (id) => {
  processandoId.value = id

  try {
    await abrirComanda(id)
  } finally {
    processandoId.value = null
  }
}

const fechar = async (id) => {
  processandoId.value = id

  try {
    await fecharComanda(id)
  } finally {
    processandoId.value = null
  }
}

const copiarAcesso = async (comanda) => {
  const link = montarLinkComanda(
      comanda.codigoAcesso
  )

  try {
    await navigator.clipboard.writeText(link)

    abrirPopup(
        'Link copiado',
        `O acesso da comanda ${comanda.numero} foi copiado.`
    )
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível copiar o link da comanda.',
        'erro'
    )
  }
}
const solicitarArquivamento = (comanda) => {
  comandaParaArquivar.value = comanda
  confirmacaoArquivamentoVisivel.value = true
}

const cancelarArquivamento = () => {
  confirmacaoArquivamentoVisivel.value = false
  comandaParaArquivar.value = null
}

const confirmarArquivamento = async () => {
  if (!comandaParaArquivar.value) {
    return
  }

  const id = comandaParaArquivar.value.id

  processandoId.value = id

  try {
    const arquivada = await arquivarComanda(id)

    if (arquivada) {
      cancelarArquivamento()
    }
  } finally {
    processandoId.value = null
  }
}
const formatarData = (data) => {
  if (!data) {
    return '—'
  }

  return new Date(data).toLocaleString(
      'pt-BR',
      {
        dateStyle: 'short',
        timeStyle: 'short'
      }
  )
}

const textoStatus = (status) =>
    status === 'EM_USO'
        ? 'Em uso'
        : 'Disponível'

const severidadeStatus = (status) =>
    status === 'EM_USO'
        ? 'warning'
        : 'success'
</script>

<template>
  <main class="container">
    <AppHeader />

    <header class="cabecalho-comandas">
      <div>
        <span class="titulo-secao">
          Atendimento
        </span>

        <h2>Comandas</h2>

        <p class="texto-secundario">
          Libere e acompanhe as comandas utilizadas
          pelos clientes.
        </p>
      </div>

      <Button
          label="Atualizar"
          icon="pi pi-refresh"
          severity="secondary"
          :loading="carregandoComandas"
          @click="carregarComandas"
      />
    </header>

    <Card class="painel nova-comanda-card">
      <template #title>
        Cadastrar comanda
      </template>

      <template #content>
        <form
            class="form-nova-comanda"
            @submit.prevent="criar"
        >
          <div class="campo">
            <label for="numero-comanda">
              Número
              <span class="campo-obrigatorio">*</span>
            </label>

            <InputNumber
                input-id="numero-comanda"
                v-model="numeroNovaComanda"
                :min="1"
                :use-grouping="false"
                placeholder="Ex.: 12"
                required
                fluid
            />
          </div>

          <Button
              type="submit"
              label="Criar comanda"
              icon="pi pi-plus"
              :loading="criandoComanda"
          />
        </form>
      </template>
    </Card>

    <p
        v-if="carregandoComandas && !comandas.length"
        class="texto-secundario"
    >
      Carregando comandas...
    </p>

    <p
        v-else-if="!comandas.length"
        class="texto-secundario"
    >
      Nenhuma comanda cadastrada.
    </p>

    <section
        v-else
        class="grade-comandas"
    >
      <Card
          v-for="comanda in comandas"
          :key="comanda.id"
          class="comanda-card"
          :class="{
            'comanda-card-em-uso':
                comanda.status === 'EM_USO'
          }"
      >
        <template #title>
          <div class="comanda-titulo">
            <span>
              Comanda {{ comanda.numero }}
            </span>

            <Tag
                :value="textoStatus(comanda.status)"
                :severity="
                  severidadeStatus(comanda.status)
                "
            />
          </div>
        </template>

        <template #content>
          <div class="detalhes-comanda">
            <div>
              <span class="rotulo-comanda">
                Código de acesso
              </span>

              <code class="codigo-comanda">
                {{ comanda.codigoAcesso }}
              </code>
            </div>

            <div>
              <span class="rotulo-comanda">
                Abertura
              </span>

              <strong>
                {{ formatarData(comanda.dataAbertura) }}
              </strong>
            </div>

            <div>
              <span class="rotulo-comanda">
                Último fechamento
              </span>

              <strong>
                {{ formatarData(comanda.dataFechamento) }}
              </strong>
            </div>
          </div>
        </template>

        <template #footer>
          <div class="acoes-comanda">
            <Button
                v-if="comanda.status === 'DISPONIVEL'"
                label="Abrir"
                icon="pi pi-lock-open"
                severity="success"
                :loading="processandoId === comanda.id"
                @click="abrir(comanda.id)"
            />

            <Button
                v-else
                label="Fechar"
                icon="pi pi-lock"
                severity="warning"
                :loading="processandoId === comanda.id"
                @click="fechar(comanda.id)"
            />

            <Button
                label="Copiar acesso"
                icon="pi pi-copy"
                severity="secondary"
                outlined
                @click="copiarAcesso(comanda)"
            />

            <Button
                label="QR Code"
                icon="pi pi-qrcode"
                severity="secondary"
                @click="mostrarQrCode(comanda)"
            />
            <Button
    label="Arquivar"
    icon="pi pi-trash"
    severity="danger"
    outlined
    :disabled="comanda.status === 'EM_USO'"
    :loading="processandoId === comanda.id"
    @click="solicitarArquivamento(comanda)"
/>
          </div>
        </template>
      </Card>
    </section>

    <Dialog
        v-model:visible="qrCodeVisivel"
        modal
        :header="`QR Code — Comanda ${
          comandaSelecionada?.numero || ''
        }`"
        :style="{ width: 'min(92vw, 420px)' }"
    >
      <div class="qr-code-conteudo">
        <div
            v-if="gerandoQrCode"
            class="qr-code-carregando"
        >
          Gerando QR Code...
        </div>

        <template v-else-if="qrCodeImagem">
          <img
              :src="qrCodeImagem"
              :alt="`QR Code da comanda ${
                comandaSelecionada?.numero
              }`"
              class="qr-code-imagem"
          >

          <p class="qr-code-instrucao">
            Escaneie para acessar o cardápio desta
            comanda.
          </p>

          <small class="qr-code-link">
            {{
              montarLinkComanda(
                  comandaSelecionada?.codigoAcesso
              )
            }}
          </small>

          <Button
              label="Copiar link"
              icon="pi pi-copy"
              class="qr-code-copiar"
              @click="copiarLinkQrCode"
          />
        </template>
      </div>
    </Dialog>
    <Dialog
    v-model:visible="confirmacaoArquivamentoVisivel"
    modal
    header="Arquivar comanda"
    :style="{ width: 'min(92vw, 420px)' }"
    @hide="comandaParaArquivar = null"
>
  <p v-if="comandaParaArquivar">
    Tem certeza que deseja arquivar a
    <strong>
      Comanda {{ comandaParaArquivar.numero }}
    </strong>?
  </p>

  <p class="texto-secundario">
    Ela deixará de aparecer na listagem de comandas.
  </p>

  <template #footer>
    <Button
        label="Cancelar"
        severity="secondary"
        text
        @click="cancelarArquivamento"
    />

    <Button
        label="Arquivar"
        icon="pi pi-trash"
        severity="danger"
        :loading="
          processandoId === comandaParaArquivar?.id
        "
        @click="confirmarArquivamento"
    />
  </template>
</Dialog>
  </main>
</template>