# Frontend do PubBunkerApp

Aplicação Nuxt 4.6.1 com Vue 3.5.44 e PrimeVue 4.3.9. Consulte o [guia do projeto](../README.md) para preparar o banco, configurar a API e executar o sistema completo.

Com Node.js e npm instalados, execute nesta pasta:

```sh
npm ci
npm run dev
```

O frontend usa a porta `3000`. A API precisa estar disponível: por padrão, é acessada no mesmo host do navegador, na porta `8080`. Para alterar as URLs, crie um `.env` com base em [.env.example](.env.example); preserve o arquivo existente se já estiver configurado.

| Comando | Finalidade |
| --- | --- |
| `npm run dev` | Desenvolvimento local |
| `npm run dev:rede` | Desenvolvimento acessível na rede, na porta 3000 |
| `npm test` | Regressões dos formulários administrativos, com API simulada |
| `npm run build` | Build de produção em `.output/` |
| `npm run preview` | Conferência local do build |

Após o build, `node .output/server/index.mjs` inicia o servidor Nuxt de produção. Configure as variáveis públicas no ambiente desse processo conforme o guia do projeto.
