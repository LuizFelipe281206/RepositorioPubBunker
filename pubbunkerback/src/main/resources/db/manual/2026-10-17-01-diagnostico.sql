-- SOMENTE LEITURA: execute antes da atualização. Não apaga nem altera dados.
-- 1. Nomes repetidos, inclusive diferenças de maiúsculas e espaços.
SELECT 'produto' AS cadastro, lower(btrim(regexp_replace(nome, U&'[[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]+', ' ', 'g'))) AS nome_normalizado, array_agg(id ORDER BY id) AS ids, array_agg(nome ORDER BY id) AS nomes
FROM produto WHERE deleted_at IS NULL
GROUP BY lower(btrim(regexp_replace(nome, U&'[[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]+', ' ', 'g'))) HAVING count(*) > 1;

SELECT 'adicionais' AS cadastro, lower(btrim(regexp_replace(nome, U&'[[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]+', ' ', 'g'))) AS nome_normalizado, array_agg(id ORDER BY id) AS ids, array_agg(nome ORDER BY id) AS nomes
FROM adicionais WHERE deleted_at IS NULL
GROUP BY lower(btrim(regexp_replace(nome, U&'[[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]+', ' ', 'g'))) HAVING count(*) > 1;

-- 2. Levantamento informativo: o catálogo permite mais de quatro vínculos.
-- Não remova vínculos por esta contagem; o limite de quatro vale por item do pedido.
SELECT p.id, p.nome, count(*) AS quantidade_adicionais
FROM produto p JOIN produto_adicionais pa ON pa.produto_id = p.id
WHERE p.deleted_at IS NULL GROUP BY p.id, p.nome HAVING count(*) > 4;

-- 3. Valores e tamanhos antigos fora dos limites: corrigir sem truncar textos.
SELECT id, nome, preco, char_length(nome) AS tamanho_nome,
       char_length(descricao) AS tamanho_descricao, categoria
FROM produto WHERE nome IS NULL OR char_length(nome) > 100
OR descricao IS NULL OR char_length(descricao) > 500
OR char_length(categoria) > 50 OR preco NOT BETWEEN 0.01 AND 1000;
SELECT id, nome, preco, char_length(nome) AS tamanho_nome FROM adicionais
WHERE nome IS NULL OR char_length(nome) > 100 OR preco NOT BETWEEN 0 AND 100;
SELECT id, nome, email FROM usuarios
WHERE nome IS NULL OR char_length(nome) > 100 OR email IS NULL OR char_length(email) > 254;

-- 4. Resumo do esquema existente (inclusive bancos de outros PCs).
SELECT table_name, column_name, data_type, character_maximum_length
FROM information_schema.columns WHERE table_schema = current_schema()
AND table_name IN ('produto','adicionais','itens_pedido','usuarios','comandas')
ORDER BY table_name, ordinal_position;

-- 5. Restrições já existentes: identificar regras antigas que conflitam com a revisão.
SELECT conrelid::regclass AS tabela, conname AS restricao,
       pg_get_constraintdef(oid) AS definicao, convalidated AS validada
FROM pg_constraint WHERE connamespace = current_schema()::regnamespace
AND conrelid IN ('produto'::regclass,'adicionais'::regclass,'pedidos'::regclass,
                'itens_pedido'::regclass,'usuarios'::regclass,'produto_adicionais'::regclass)
ORDER BY conrelid::regclass::text, conname;

-- 6. Pedidos legados com total/subtotais incoerentes, inclusive arquivados.
SELECT p.id, p.valor_total, coalesce(sum(i.subtotal),0) AS soma_itens,
       count(i.id) AS quantidade_itens
FROM pedidos p LEFT JOIN itens_pedido i ON i.pedido_id=p.id
GROUP BY p.id,p.valor_total
HAVING p.valor_total IS NULL OR p.valor_total NOT BETWEEN 0.01 AND 10000
    OR p.valor_total<>coalesce(sum(i.subtotal),0)
    OR count(i.id) NOT BETWEEN 1 AND 50;
SELECT id,pedido_id,quantidade,preco_unitario,subtotal FROM itens_pedido
WHERE quantidade IS NULL OR quantidade NOT BETWEEN 1 AND 100
   OR preco_unitario IS NULL OR preco_unitario NOT BETWEEN 0.01 AND 10000
   OR subtotal IS NULL OR subtotal NOT BETWEEN 0.01 AND 10000
   OR subtotal<>preco_unitario*quantidade;

-- 7. Categorias e campos obrigatórios antigos que precisam de correção manual.
SELECT id,nome,categoria FROM produto WHERE deleted_at IS NULL
AND (categoria IS NULL OR categoria NOT IN ('Bebidas','Porções','Lanches')
 OR nome IS NULL OR nome !~ U&'[^[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]'
 OR descricao IS NULL OR descricao !~ U&'[^[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]'
 OR preco IS NULL);
SELECT id,nome,preco FROM adicionais WHERE deleted_at IS NULL
AND (nome IS NULL OR nome !~ U&'[^[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]' OR preco IS NULL);
