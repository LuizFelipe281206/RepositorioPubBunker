import { readFile } from 'node:fs/promises'
import vm from 'node:vm'
import test from 'node:test'
import assert from 'node:assert/strict'
import * as Vue from 'vue'
import { parse, compileScript } from 'vue/compiler-sfc'
import { renderToString } from '@vue/server-renderer'
import { mensagemErro, prepararInclusao, validarCarrinho } from '../app/utils/catalogo.js'

const codigo='11111111-1111-1111-1111-111111111111'
const pedido={id:71,itens:[],status:'PENDENTE',valorTotal:10}
const avaliacao={id:41,nota:5,comentario:'Avaliação existente'}
const falha=(status=500,mensagem='Não foi possível consultar agora.')=>Object.assign(new Error(mensagem),{response:{status,data:{mensagem}}})
function pendente(){let resolve,reject;const promise=new Promise((a,b)=>{resolve=a;reject=b});return {promise,resolve,reject}}

async function cliente(api={}) {
 const estado=new Map(),popups=[]
 const useState=(key,init)=>{if(!estado.has(key))estado.set(key,Vue.ref(init()));return estado.get(key)}
 const contexto=vm.createContext({
  ...Vue,useState,useNuxtApp:()=>({$api:api}),usePopup:()=>({abrirPopup:(...args)=>popups.push(args)}),
  mensagemErro,prepararInclusao,validarCarrinho,
  definePageMeta:()=>{},navigateTo:()=>{},onMounted:()=>{},onBeforeUnmount:()=>{},
  sessionStorage:{setItem:()=>{},removeItem:()=>{}},window:{scrollTo:()=>{}},setInterval:()=>1,clearInterval:()=>{}
 })
 for(const name of ['useComanda','usePedidosComanda','useCarrinho','useAvaliacoes']){
  let source=await readFile(new URL('../app/composables/'+name+'.js',import.meta.url),'utf8')
  source=source.replace(/^import .+\r?\n/gm,'').replace('export const ','const ').replaceAll('import.meta.client','true').replaceAll('import.meta.server','false')
  vm.runInContext(`(()=>{${source}\nglobalThis.${name}=${name}})()`,contexto)
 }
 contexto.useComanda().comandaAtual.value={id:1,numero:1,codigoAcesso:codigo,status:'EM_USO'}
 return {contexto,estado,popups,useState,
  pedidos:contexto.usePedidosComanda(),carrinho:contexto.useCarrinho(),avaliacoes:contexto.useAvaliacoes(),comanda:contexto.useComanda()}
}

async function tela(nome,ctx){
 const source=await readFile(new URL('../app/pages/'+nome+'.vue',import.meta.url),'utf8')
 const {descriptor,errors}=parse(source);assert.deepEqual(errors,[])
 const {bindings}=compileScript(descriptor,{id:'cliente-'+nome})
 const setup=vm.runInContext(`(()=>{${descriptor.scriptSetup.content}\nreturn {${Object.keys(bindings).join(',')}}})()`,ctx)
 const Button=Vue.defineComponent({props:['label'],setup:props=>()=>Vue.h('button',props.label)})
 const vazio=Vue.defineComponent({setup:()=>()=>Vue.h('div')})
 const app=Vue.createSSRApp({setup:()=>setup,render:Vue.compile(descriptor.template.content),components:{AppHeader:vazio,CartPanel:vazio,Card:vazio,Button,ProgressSpinner:vazio,InputText:vazio,Rating:vazio,Textarea:vazio,Dialog:vazio,Tag:vazio}})
 app.config.warnHandler=()=>{}
 return renderToString(app)
}

test('consulta iniciada antes do envio não apaga o pedido confirmado',async()=>{
 const antigo=pendente();const c=await cliente({get:()=>antigo.promise,post:async()=>({data:pedido})})
 const consulta=c.pedidos.carregarPedidosComanda()
 c.carrinho.adicionar({id:1,nome:'Produto',preco:10,ativo:true})
 await c.carrinho.finalizar()
 antigo.resolve({data:[]});await consulta
 assert.equal(c.pedidos.pedidosComanda.value.length,1)
 assert.equal(c.pedidos.pedidosComanda.value[0].id,pedido.id)
})

test('falha tardia de uma consulta antiga não cobre a confirmação do envio',async()=>{
 const antigo=pendente();const c=await cliente({get:()=>antigo.promise,post:async()=>({data:pedido})})
 const consulta=c.pedidos.carregarPedidosComanda()
 c.carrinho.adicionar({id:1,nome:'Produto',preco:10,ativo:true})
 await c.carrinho.finalizar();const antes=c.popups.length
 antigo.reject(falha());await consulta
 assert.equal(c.popups.length,antes)
 assert.equal(c.popups.at(-1)[0],'Pedido realizado')
})

test('resposta mais antiga não regride o estado do pedido',async()=>{
 const a=pendente(),b=pendente();let n=0
 const c=await cliente({get:()=>++n===1?a.promise:b.promise})
 const primeira=c.pedidos.carregarPedidosComanda(),segunda=c.pedidos.carregarPedidosComanda()
 b.resolve({data:[{...pedido,status:'EM_PREPARO'}]});await segunda
 a.resolve({data:[pedido]});await primeira
 assert.equal(c.pedidos.pedidosComanda.value[0].status,'EM_PREPARO')
})

test('falha de consulta mantém pedidos já exibidos e apresenta o motivo',async()=>{
 const c=await cliente({get:async()=>{throw falha(429,'Aguarde antes de consultar novamente.')}})
 c.pedidos.pedidosComanda.value=[pedido]
 await c.pedidos.carregarPedidosComanda()
 assert.equal(c.pedidos.pedidosComanda.value.length,1)
 assert.match(c.pedidos.erroPedidosComanda?.value||'',/Aguarde/)
 assert.match(c.popups.at(-1)[1],/Aguarde/)
})

test('falha de consulta mantém avaliações já exibidas e permite tentar de novo',async()=>{
 const c=await cliente({get:async()=>{throw falha()}})
 c.avaliacoes.avaliacoes.value=[avaliacao]
 await c.avaliacoes.carregarAvaliacoes()
 assert.equal(c.avaliacoes.avaliacoes.value.length,1)
 assert.match(c.avaliacoes.erroAvaliacoes?.value||'',/consultar agora/)
})

test('erro ao carregar pedidos não aparece como histórico vazio',async()=>{
 const c=await cliente({get:async()=>{throw falha()}})
 await c.pedidos.carregarPedidosComanda()
 const html=await tela('pedidos',c.contexto)
 assert.doesNotMatch(html,/Nenhum pedido foi encontrado/)
 assert.match(html,/Não foi possível consultar agora/)
})

test('erro ao carregar avaliações não aparece como ausência de avaliações',async()=>{
 const c=await cliente({get:async()=>{throw falha()}})
 await c.avaliacoes.carregarAvaliacoes()
 const html=await tela('avaliacoes',c.contexto)
 assert.doesNotMatch(html,/Ainda não há avaliações/)
 assert.match(html,/Não foi possível consultar agora/)
 assert.match(html,/Tentar novamente/)
})

test('envio recusado preserva carrinho e observação para a tentativa seguinte',async()=>{
 const c=await cliente({post:async()=>{throw falha(400,'Confira os adicionais selecionados.')}})
 c.carrinho.adicionar({id:1,nome:'Produto',preco:10,ativo:true});c.carrinho.observacao.value='Sem cebola'
 await c.carrinho.finalizar()
 assert.equal(c.carrinho.carrinho.value.length,1);assert.equal(c.carrinho.observacao.value,'Sem cebola')
 assert.equal(c.carrinho.enviandoPedido.value,false);assert.match(c.popups.at(-1)[1],/adicionais/)
})

test('atualização silenciosa falha sem abrir popup nem apagar pedidos',async()=>{
 const c=await cliente({get:async()=>{throw falha()}})
 c.pedidos.pedidosComanda.value=[pedido]
 await c.pedidos.carregarPedidosComanda(true)
 assert.equal(c.popups.length,0)
 assert.equal(c.pedidos.pedidosComanda.value[0].id,pedido.id)
 assert.ok(c.pedidos.erroPedidosComanda.value)
})

test('limite de consultas usa o tempo de espera informado pelo servidor',async()=>{
 const erro=falha(429,'Aguarde para consultar.');erro.response.data.aguardeSegundos=7
 const c=await cliente({get:async()=>{throw erro}})
 await c.pedidos.carregarPedidosComanda()
 assert.equal(c.popups.at(-1)[3],7)
})

for(const [nome,acesso,carregar,erro] of [
 ['pedidos','pedidos','carregarPedidosComanda','erroPedidosComanda'],
 ['avaliacoes','avaliacoes','carregarAvaliacoes','erroAvaliacoes']
]){
 test('nova consulta de '+nome+' remove o aviso após resposta válida',async()=>{
  let chamadas=0
  const c=await cliente({get:async()=>{if(!chamadas++)throw falha();return {data:[]}}})
  await c[acesso][carregar]()
  assert.ok(c[acesso][erro].value)
  await c[acesso][carregar]()
  assert.equal(c[acesso][erro].value,null)
  const html=await tela(nome,c.contexto)
  assert.match(html,nome==='pedidos'?/Nenhum pedido foi encontrado/:/Ainda não há avaliações/)
 })
}

test('encerrar o atendimento limpa erros e descarta consultas em andamento',async()=>{
 const a=pendente(),b=pendente();let chamadas=0
 const c=await cliente({get:()=>++chamadas===1?a.promise:b.promise})
 c.pedidos.erroPedidosComanda.value='Erro anterior'
 c.avaliacoes.erroAvaliacoes.value='Erro anterior'
 const cp=c.pedidos.carregarPedidosComanda(),ca=c.avaliacoes.carregarAvaliacoes()
 c.comanda.limparComanda()
 a.resolve({data:[pedido]});b.resolve({data:[avaliacao]});await Promise.all([cp,ca])
 assert.equal(c.pedidos.pedidosComanda.value.length,0)
 assert.equal(c.avaliacoes.avaliacoes.value.length,0)
 assert.equal(c.pedidos.erroPedidosComanda.value,null)
 assert.equal(c.avaliacoes.erroAvaliacoes.value,null)
 assert.equal(c.pedidos.carregandoPedidosComanda.value,false)
 assert.equal(c.avaliacoes.carregandoAvaliacoes.value,false)
})
