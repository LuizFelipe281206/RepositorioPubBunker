import { readFile } from 'node:fs/promises'
import vm from 'node:vm'
import assert from 'node:assert/strict'
import test from 'node:test'
import { ref, reactive, computed } from 'vue'
import { parse } from 'vue/compiler-sfc'
import { mensagemErro } from '../app/utils/catalogo.js'

// Executa o setup real dos componentes com a API simulada, sem servidor ou banco.
async function carregarTela(arquivo, nomes, api = {}) {
  const fonte = await readFile(new URL(`../app/pages/admin/${arquivo}`, import.meta.url), 'utf8')
  const { descriptor, errors } = parse(fonte)
  assert.deepEqual(errors, [])
  const script = descriptor.scriptSetup.content.replace(
    /^import \{ mensagemErro \} from '~\/utils\/catalogo\.js'\r?$/m,
    ''
  )
  const popups = []
  const contexto = vm.createContext({
    ref, reactive, computed, mensagemErro,
    definePageMeta: () => {},
    useNuxtApp: () => ({ $api: api }),
    useAuth: () => ({ usuarioId: ref(1), role: ref('ADMIN') }),
    usePopup: () => ({ abrirPopup: (...args) => popups.push(args) }),
    onMounted: () => {}, onBeforeUnmount: () => {}, watch: () => {}
  })
  vm.runInContext(`${script}\nglobalThis.tela = {${nomes.join(',')}}`, contexto)
  return { ...contexto.tela, popups }
}

const camposFuncionario = [
  'funcionarios', 'abrirModal', 'fecharModal', 'formulario', 'confirmar',
  'emailLegado', 'usuarioEmail', 'erroFormulario', 'modalVisivel', 'processando'
]

async function editarFuncionario(email = 'legado@exemplo.com', api = {}) {
  const tela = await carregarTela('funcionarios.vue', camposFuncionario, api)
  tela.funcionarios.value = [{ id: 7, nome: 'Funcionário', email }]
  tela.abrirModal('editar', tela.funcionarios.value[0])
  return tela
}

test('email legado pode ser corrigido sem duplicar o domínio nem mudar a conta antes de salvar', async () => {
  const requisicoes = []
  const tela = await editarFuncionario(undefined, {
    put: async (url, dados) => {
      requisicoes.push({ url, dados })
      return { data: { id: 7, ...dados } }
    }
  })
  assert.equal(tela.emailLegado.value, true)
  assert.equal(tela.formulario.email, 'legado@exemplo.com')
  tela.formulario.email = '  Corrigido@PubBunker.com  '
  assert.equal(tela.funcionarios.value[0].email, 'legado@exemplo.com')
  await tela.confirmar()
  assert.equal(requisicoes.length, 1)
  assert.equal(requisicoes[0].url, '/funcionarios/7')
  assert.equal(requisicoes[0].dados.email, 'corrigido@pubbunker.com')
  assert.equal(tela.funcionarios.value[0].email, 'corrigido@pubbunker.com')
  assert.equal(tela.modalVisivel.value, false)
})

test('alterar só o nome de cadastro legado pede a correção do email antes de chamar a API', async () => {
  let requisicoes = 0
  const tela = await editarFuncionario(undefined, { put: async () => { requisicoes++ } })
  tela.formulario.nome = 'Novo nome'
  await tela.confirmar()
  assert.equal(requisicoes, 0)
  assert.match(tela.erroFormulario.value, /@pubbunker\.com/)
  assert.equal(tela.modalVisivel.value, true)
  assert.equal(tela.formulario.nome, 'Novo nome')
})

for (const [descricao, email] of [
  ['vazio', ''],
  ['outro domínio', 'pessoa@exemplo.com'],
  ['domínio duplicado', 'pessoa@pubbunker.com@exemplo.com'],
  ['espaço no usuário', 'pes soa@pubbunker.com'],
  ['mais de 254 caracteres', 'a'.repeat(242) + '@pubbunker.com']
]) {
  test(`email ${descricao} é recusado antes de enviar`, async () => {
    let requisicoes = 0
    const tela = await editarFuncionario(undefined, { put: async () => { requisicoes++ } })
    tela.formulario.email = email
    await tela.confirmar()
    assert.equal(requisicoes, 0)
    assert.match(tela.erroFormulario.value, /email válido/)
    assert.equal(tela.modalVisivel.value, true)
  })
}

test('edição com domínio atual aceita usuário ou endereço completo sem repetir o sufixo', async () => {
  let enviado
  const tela = await editarFuncionario('antigo@pubbunker.com', {
    put: async (_url, dados) => {
      enviado = dados
      return { data: { id: 7, ...dados } }
    }
  })
  assert.equal(tela.emailLegado.value, false)
  assert.equal(tela.usuarioEmail.value, 'antigo')
  tela.usuarioEmail.value = 'novo'
  assert.equal(tela.formulario.email, 'novo@pubbunker.com')
  tela.usuarioEmail.value = ' Novo@PubBunker.com '
  await tela.confirmar()
  assert.equal(enviado.email, 'novo@pubbunker.com')
})

test('cadastro novo mantém domínio fixo e envia senha somente na criação', async () => {
  let enviado
  const tela = await carregarTela('funcionarios.vue', camposFuncionario, {
    post: async (url, dados) => {
      assert.equal(url, '/funcionarios')
      enviado = dados
      return { data: { id: 8, nome: dados.nome, email: dados.email } }
    }
  })
  tela.abrirModal('criar')
  assert.equal(tela.emailLegado.value, false)
  tela.formulario.nome = 'Novo funcionário'
  tela.usuarioEmail.value = 'novo'
  tela.formulario.senha = 'SenhaNova#123'
  await tela.confirmar()
  assert.equal(enviado.email, 'novo@pubbunker.com')
  assert.equal(enviado.senha, 'SenhaNova#123')
  assert.equal(tela.modalVisivel.value, false)
})

test('falha de email repetido preserva a correção digitada e mostra a mensagem da API', async () => {
  const tela = await editarFuncionario(undefined, {
    put: async () => {
      throw { response: { status: 400, data: { campos: { email: 'Este email já está em uso.' } } } }
    }
  })
  tela.formulario.email = 'existente@pubbunker.com'
  await tela.confirmar()
  assert.equal(tela.erroFormulario.value, 'Este email já está em uso.')
  assert.equal(tela.formulario.email, 'existente@pubbunker.com')
  assert.equal(tela.funcionarios.value[0].email, 'legado@exemplo.com')
  assert.equal(tela.modalVisivel.value, true)
  assert.equal(tela.processando.value, false)
})

test('cancelar a correção não altera o email do funcionário', async () => {
  const tela = await editarFuncionario()
  tela.formulario.email = 'corrigido@pubbunker.com'
  tela.fecharModal()
  assert.equal(tela.funcionarios.value[0].email, 'legado@exemplo.com')
  assert.equal(tela.modalVisivel.value, false)
})

async function prepararAvaliacao(arquivada, api) {
  const tela = await carregarTela('avaliacoes.vue', [
    'avaliacoes', 'mostrarArquivadas', 'solicitarAcao', 'confirmarAcao',
    'confirmacaoVisivel', 'processando'
  ], api)
  tela.mostrarArquivadas.value = arquivada
  tela.avaliacoes.value = [{ id: 9, arquivadoEm: arquivada ? '2026-10-01T12:00:00' : null }]
  tela.solicitarAcao(tela.avaliacoes.value[0])
  return tela
}

for (const arquivada of [true, false]) {
  const verbo = arquivada ? 'excluir' : 'arquivar'
  const metodo = arquivada ? 'delete' : 'patch'
  const rota = arquivada ? '/avaliacoes/arquivadas/9' : '/avaliacoes/9/arquivar'

  test(`falha ao ${verbo} avaliação mostra a ação correta e preserva a lista`, async () => {
    const tela = await prepararAvaliacao(arquivada, {
      [metodo]: async url => {
        assert.equal(url, rota)
        throw { response: { status: 409, data: {} } }
      }
    })
    await tela.confirmarAcao()
    assert.equal(tela.popups[0][0], `Não foi possível ${verbo} a avaliação`)
    assert.equal(tela.popups[0][1], `Não foi possível ${verbo} a avaliação.`)
    assert.equal(tela.avaliacoes.value.length, 1)
    assert.equal(tela.confirmacaoVisivel.value, true)
    assert.equal(tela.processando.value, false)
  })

  test(`${verbo} avaliação com sucesso retira o registro da lista e confirma a ação`, async () => {
    const tela = await prepararAvaliacao(arquivada, {
      [metodo]: async url => { assert.equal(url, rota) }
    })
    await tela.confirmarAcao()
    assert.equal(tela.avaliacoes.value.length, 0)
    assert.equal(tela.confirmacaoVisivel.value, false)
    assert.equal(tela.popups[0][0], arquivada ? 'Avaliação excluída' : 'Avaliação arquivada')
  })
}

test('limite de requisições na exclusão preserva a mensagem e o tempo de espera', async () => {
  const tela = await prepararAvaliacao(true, {
    delete: async () => {
      throw { response: { status: 429, data: { mensagem: 'Aguarde antes de tentar novamente.' } } }
    }
  })
  await tela.confirmarAcao()
  assert.equal(tela.popups[0][0], 'Não foi possível excluir a avaliação')
  assert.equal(tela.popups[0][1], 'Aguarde antes de tentar novamente.')
  assert.equal(tela.popups[0][2], 'erro')
  assert.equal(tela.popups[0][3], 60)
})
