<script setup>
definePageMeta({
  roles: ['ADMIN', 'FUNCIONARIO']
})

const {
  reservas,
  carregandoReservas,
  carregarReservas,
  criarReserva,
  atualizarReserva,
  atualizarStatusReserva,
  cancelarReserva,
  excluirReserva
} = useReservas()

const { role } = useAuth()
const { abrirPopup } = usePopup()

const reservaEditandoId = ref(null)
const filtroStatus = ref('TODAS')

const formularioVazio = () => ({
  nomeCliente: '',
  quantidadePessoas: 1,
  dataHora: ''
})

const form = reactive(formularioVazio())

const opcoesStatus = [
  {
    label: 'Todas',
    value: 'TODAS'
  },
  {
    label: 'Reservadas',
    value: 'RESERVADA'
  },
  {
    label: 'Confirmadas',
    value: 'CONFIRMADA'
  },
  {
    label: 'Concluídas',
    value: 'CONCLUIDA'
  },
  {
    label: 'Canceladas',
    value: 'CANCELADA'
  }
]

const reservasFiltradas = computed(() => {
  if (filtroStatus.value === 'TODAS') {
    return reservas.value
  }

  return reservas.value.filter(
      reserva =>
          reserva.status === filtroStatus.value
  )
})

const dataMinima = computed(() => {
  const agora = new Date()

  agora.setMinutes(
      agora.getMinutes() -
      agora.getTimezoneOffset()
  )

  return agora.toISOString().slice(0, 16)
})

onMounted(carregarReservas)

const limparFormulario = () => {
  Object.assign(
      form,
      formularioVazio()
  )

  reservaEditandoId.value = null
}

const salvar = async () => {
  if (
      !form.nomeCliente.trim() ||
      !form.quantidadePessoas ||
      !form.dataHora
  ) {
    abrirPopup(
        'Campos obrigatórios',
        'Preencha todos os dados da reserva.',
        'erro'
    )

    return
  }

  if (Number(form.quantidadePessoas) < 1) {
    abrirPopup(
        'Quantidade inválida',
        'A reserva deve possuir pelo menos uma pessoa.',
        'erro'
    )

    return
  }

  if (new Date(form.dataHora) <= new Date()) {
    abrirPopup(
        'Data inválida',
        'A data da reserva deve estar no futuro.',
        'erro'
    )

    return
  }

  try {
    if (reservaEditandoId.value) {
      await atualizarReserva(
          reservaEditandoId.value,
          form
      )

      abrirPopup(
          'Reserva atualizada',
          'Os dados da reserva foram atualizados.'
      )
    } else {
      await criarReserva(form)

      abrirPopup(
          'Reserva cadastrada',
          'A reserva foi cadastrada com sucesso.'
      )
    }

    limparFormulario()
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível salvar a reserva.',
        'erro'
    )
  }
}

const editar = (reserva) => {
  reservaEditandoId.value = reserva.id

  form.nomeCliente = reserva.nomeCliente

  form.quantidadePessoas =
      reserva.quantidadePessoas

  form.dataHora =
      reserva.dataHora.slice(0, 16)

  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const alterarStatus = async (
    reserva,
    novoStatus
) => {
  try {
    await atualizarStatusReserva(
        reserva.id,
        novoStatus
    )

    abrirPopup(
        'Status atualizado',
        `A reserva agora está como ${novoStatus}.`
    )
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível atualizar o status.',
        'erro'
    )
  }
}

const cancelar = async (reserva) => {
  try {
    await cancelarReserva(reserva.id)

    if (
        reservaEditandoId.value ===
        reserva.id
    ) {
      limparFormulario()
    }

    abrirPopup(
        'Reserva cancelada',
        'A reserva foi cancelada e mantida no histórico.'
    )
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível cancelar a reserva.',
        'erro'
    )
  }
}

const arquivar = async (reserva) => {
  try {
    await excluirReserva(reserva.id)

    abrirPopup(
        'Reserva arquivada',
        'A reserva foi removida da listagem.'
    )
  } catch {
    abrirPopup(
        'Erro',
        'Não foi possível arquivar a reserva.',
        'erro'
    )
  }
}

const formatarData = valor =>
    new Date(valor).toLocaleString('pt-BR')

const statusLabel = status => ({
  RESERVADA: 'Reservada',
  CONFIRMADA: 'Confirmada',
  CONCLUIDA: 'Concluída',
  CANCELADA: 'Cancelada'
})[status] || status

const statusSeveridade = status => ({
  RESERVADA: 'warn',
  CONFIRMADA: 'info',
  CONCLUIDA: 'success',
  CANCELADA: 'danger'
})[status] || 'secondary'
</script>

<template>
  <main class="container">
    <AppHeader />

    <Card class="painel">
      <template #title>
        {{
          reservaEditandoId
              ? 'Editar reserva'
              : 'Cadastrar reserva'
        }}
      </template>

      <template #content>
        <div class="form-grid">
          <div class="campo campo-largo">
            <label for="nome-cliente">
  Nome do cliente
  <span class="campo-obrigatorio">*</span>
</label>

            <InputText
                id="nome-cliente"
                v-model="form.nomeCliente"
                maxlength="100"
                fluid
            />
          </div>

          <div class="campo">
            <label for="quantidade-pessoas">
  Quantidade de pessoas
  <span class="campo-obrigatorio">*</span>
</label>

            <InputNumber
                id="quantidade-pessoas"
                v-model="form.quantidadePessoas"
                :min="1"
                :use-grouping="false"
                fluid
            />
          </div>

          <div class="campo">
            <label for="data-hora">
  Data e hora
  <span class="campo-obrigatorio">*</span>
</label>

            <InputText
                id="data-hora"
                v-model="form.dataHora"
                type="datetime-local"
                :min="dataMinima"
                fluid
            />
          </div>
        </div>
      </template>

      <template #footer>
        <div class="acoes-card">
          <Button
              :label="
                reservaEditandoId
                    ? 'Atualizar reserva'
                    : 'Cadastrar reserva'
              "
              icon="pi pi-save"
              @click="salvar"
          />

          <Button
              v-if="reservaEditandoId"
              label="Cancelar edição"
              severity="secondary"
              @click="limparFormulario"
          />
        </div>
      </template>
    </Card>

    <section>
      <div class="cabecalho-listagem">
        <h2>Reservas cadastradas</h2>

        <Select
            v-model="filtroStatus"
            :options="opcoesStatus"
            option-label="label"
            option-value="value"
            placeholder="Filtrar por status"
        />
      </div>

      <p
          v-if="carregandoReservas"
          class="texto-secundario"
      >
        Carregando reservas...
      </p>

      <p
          v-else-if="reservasFiltradas.length === 0"
          class="texto-secundario"
      >
        Nenhuma reserva encontrada.
      </p>

      <div
          v-else
          class="grade-reservas"
      >
        <Card
            v-for="reserva in reservasFiltradas"
            :key="reserva.id"
        >
          <template #title>
            {{ reserva.nomeCliente }}
          </template>

          <template #subtitle>
            Reserva #{{ reserva.id }}
          </template>

          <template #content>
            <div class="detalhes-reserva">
              <Tag
                  :value="statusLabel(reserva.status)"
                  :severity="
                    statusSeveridade(reserva.status)
                  "
              />

              <p>
                <strong>Data:</strong>
                {{ formatarData(reserva.dataHora) }}
              </p>

              <p>
                <strong>Pessoas:</strong>
                {{ reserva.quantidadePessoas }}
              </p>

              <p>
                <strong>Registrada por:</strong>
                {{ reserva.funcionarioNome }}
              </p>
            </div>
          </template>

          <template #footer>
            <div class="acoes-card">
              <Button
                  v-if="
                    reserva.status === 'RESERVADA' ||
                    reserva.status === 'CONFIRMADA'
                  "
                  label="Editar"
                  icon="pi pi-pencil"
                  severity="warn"
                  @click="editar(reserva)"
              />

              <Button
                  v-if="
                    reserva.status === 'RESERVADA'
                  "
                  label="Confirmar"
                  icon="pi pi-check"
                  severity="info"
                  @click="
                    alterarStatus(
                        reserva,
                        'CONFIRMADA'
                    )
                  "
              />

              <Button
                  v-if="
                    reserva.status === 'CONFIRMADA'
                  "
                  label="Concluir"
                  icon="pi pi-check-circle"
                  severity="success"
                  @click="
                    alterarStatus(
                        reserva,
                        'CONCLUIDA'
                    )
                  "
              />

              <Button
                  v-if="
                    reserva.status === 'RESERVADA' ||
                    reserva.status === 'CONFIRMADA'
                  "
                  label="Cancelar reserva"
                  icon="pi pi-times"
                  severity="danger"
                  @click="cancelar(reserva)"
              />

              <Button
                  v-if="
                    role === 'ADMIN' &&
                    (
                      reserva.status === 'CONCLUIDA' ||
                      reserva.status === 'CANCELADA'
                    )
                  "
                  label="Arquivar"
                  icon="pi pi-box"
                  severity="secondary"
                  @click="arquivar(reserva)"
              />
            </div>
          </template>
        </Card>
      </div>
    </section>
  </main>
</template>