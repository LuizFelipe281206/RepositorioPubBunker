-- Execute o diagnóstico primeiro. Pare o backend ao aplicar este bloco completo.
-- Não há exclusão de registros, renomeação automática de duplicatas ou truncamento.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '60s';

CREATE OR REPLACE FUNCTION nome_catalogo_chave(text) RETURNS text
LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE AS $$
 SELECT lower(btrim(regexp_replace($1,
   U&'[[:space:]\00A0\1680\2000-\200A\2028\2029\202F\205F\3000\FEFF]+', ' ', 'g')))
$$;

DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM produto WHERE deleted_at IS NULL GROUP BY nome_catalogo_chave(nome) HAVING count(*) > 1)
 OR EXISTS (SELECT 1 FROM adicionais WHERE deleted_at IS NULL GROUP BY nome_catalogo_chave(nome) HAVING count(*) > 1) THEN
   RAISE EXCEPTION 'Há nomes repetidos no catálogo. Corrija os registros apontados pelo diagnóstico antes de aplicar a atualização.';
 END IF;
 IF EXISTS (SELECT 1 FROM produto WHERE char_length(nome)>100 OR char_length(descricao)>500 OR char_length(categoria)>50)
 OR EXISTS (SELECT 1 FROM adicionais WHERE char_length(nome)>100)
 OR EXISTS (SELECT 1 FROM usuarios WHERE char_length(nome)>100 OR char_length(email)>254) THEN
   RAISE EXCEPTION 'Há textos antigos maiores que os limites. Revise o diagnóstico; nenhum texto foi cortado.';
 END IF;
END $$;

-- Compatibilidade com os campos recentes, também nos bancos dos outros PCs.
ALTER TABLE comandas ADD COLUMN IF NOT EXISTS codigo_entrada varchar(5);
ALTER TABLE comandas ADD COLUMN IF NOT EXISTS primeiro_acesso_realizado boolean NOT NULL DEFAULT false;
ALTER TABLE itens_pedido ADD COLUMN IF NOT EXISTS produto_nome varchar(255);
UPDATE itens_pedido i SET produto_nome = p.nome FROM produto p
WHERE i.produto_id = p.id AND i.produto_nome IS NULL;
ALTER TABLE itens_pedido ALTER COLUMN produto_nome SET NOT NULL;
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_role_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_role_check
 CHECK (role IN ('ADMIN','FUNCIONARIO','CLIENTE')) NOT VALID;

ALTER TABLE produto ALTER COLUMN nome TYPE varchar(100),
 ALTER COLUMN descricao TYPE varchar(500), ALTER COLUMN categoria TYPE varchar(50);
ALTER TABLE adicionais ALTER COLUMN nome TYPE varchar(100);
ALTER TABLE adicionais ADD COLUMN IF NOT EXISTS categoria varchar(50);
ALTER TABLE usuarios ALTER COLUMN nome TYPE varchar(100), ALTER COLUMN email TYPE varchar(254);

DROP INDEX IF EXISTS uk_produto_nome_nao_excluido;
CREATE UNIQUE INDEX uk_produto_nome_nao_excluido ON produto (nome_catalogo_chave(nome)) WHERE deleted_at IS NULL;
DROP INDEX IF EXISTS uk_adicional_nome_nao_excluido;
CREATE UNIQUE INDEX uk_adicional_nome_nao_excluido ON adicionais (nome_catalogo_chave(nome)) WHERE deleted_at IS NULL;

-- CHECK NOT VALID preserva o legado e valida toda nova inserção/alteração.
-- Categorias antigas ficam a classificar; nenhuma categoria é adivinhada.
ALTER TABLE produto DROP CONSTRAINT IF EXISTS ck_produto_catalogo;
ALTER TABLE produto ADD CONSTRAINT ck_produto_catalogo CHECK (deleted_at IS NOT NULL OR (
 nome IS NOT NULL AND nome_catalogo_chave(nome)<>'' AND char_length(nome)<=100
 AND descricao IS NOT NULL AND nome_catalogo_chave(descricao)<>'' AND char_length(descricao)<=500
 AND categoria IS NOT NULL AND categoria IN ('Bebidas','Porções','Lanches')
 AND preco IS NOT NULL AND preco BETWEEN 0.01 AND 1000)) NOT VALID;
ALTER TABLE adicionais DROP CONSTRAINT IF EXISTS ck_adicional_catalogo;
ALTER TABLE adicionais ADD CONSTRAINT ck_adicional_catalogo CHECK (deleted_at IS NOT NULL OR (
 nome IS NOT NULL AND nome_catalogo_chave(nome)<>'' AND char_length(nome)<=100
 AND categoria IS NOT NULL AND categoria IN ('Bebidas','Porções','Lanches')
 AND preco IS NOT NULL AND preco BETWEEN 0 AND 100)) NOT VALID;
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS ck_usuario_textos;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuario_textos CHECK (
 nome IS NOT NULL AND nome_catalogo_chave(nome)<>'' AND char_length(nome)<=100
 AND email IS NOT NULL AND btrim(email)<>'' AND char_length(email)<=254) NOT VALID;
ALTER TABLE pedidos DROP CONSTRAINT IF EXISTS ck_pedido_limites;
ALTER TABLE pedidos ADD CONSTRAINT ck_pedido_limites CHECK (
 valor_total IS NOT NULL AND valor_total BETWEEN 0.01 AND 10000
 AND (observacao IS NULL OR char_length(observacao)<=500)) NOT VALID;
ALTER TABLE itens_pedido DROP CONSTRAINT IF EXISTS ck_item_pedido_limites;
ALTER TABLE itens_pedido ADD CONSTRAINT ck_item_pedido_limites CHECK (
 quantidade IS NOT NULL AND quantidade BETWEEN 1 AND 100
 AND preco_unitario IS NOT NULL AND preco_unitario BETWEEN 0.01 AND 10000
 AND subtotal IS NOT NULL AND subtotal BETWEEN 0.01 AND 10000
 AND subtotal=preco_unitario*quantidade AND produto_nome IS NOT NULL AND char_length(produto_nome)<=255) NOT VALID;
ALTER TABLE avaliacoes DROP CONSTRAINT IF EXISTS ck_avaliacao_limites;
ALTER TABLE avaliacoes ADD CONSTRAINT ck_avaliacao_limites CHECK (
 nota IS NOT NULL AND nota BETWEEN 1 AND 5
 AND (nome_cliente IS NULL OR char_length(nome_cliente)<=100)
 AND comentario IS NOT NULL AND char_length(comentario)<=255) NOT VALID;
ALTER TABLE reservas DROP CONSTRAINT IF EXISTS ck_reserva_limites;
ALTER TABLE reservas ADD CONSTRAINT ck_reserva_limites CHECK (
 nome_cliente IS NOT NULL AND nome_catalogo_chave(nome_cliente)<>'' AND char_length(nome_cliente)<=100
 AND quantidade_pessoas IS NOT NULL AND quantidade_pessoas>=1) NOT VALID;

-- No fim da transação: permite substituir os quatro vínculos sem falhar
-- no estado intermediário. A trava do produto serializa as alterações.
CREATE OR REPLACE FUNCTION validar_quatro_adicionais_produto() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE alvo bigint;
BEGIN
 alvo := NEW.produto_id;
 PERFORM 1 FROM produto WHERE id=alvo FOR UPDATE;
 IF (SELECT count(*) FROM produto_adicionais WHERE produto_id=alvo)>4 THEN
   RAISE EXCEPTION 'Selecione no máximo 4 adicionais por produto.'
     USING ERRCODE='23514', CONSTRAINT='ck_produto_quatro_adicionais';
 END IF;
 RETURN NULL;
END $$;
DROP TRIGGER IF EXISTS ck_produto_quatro_adicionais ON produto_adicionais;
CREATE CONSTRAINT TRIGGER ck_produto_quatro_adicionais
 AFTER INSERT OR UPDATE ON produto_adicionais DEFERRABLE INITIALLY DEFERRED
 FOR EACH ROW EXECUTE FUNCTION validar_quatro_adicionais_produto();

-- Valida a soma dos itens quando pedido e itens já foram gravados pelo JPA.
CREATE OR REPLACE FUNCTION conferir_total_pedido(alvo bigint) RETURNS void
LANGUAGE plpgsql AS $$
DECLARE informado numeric; calculado numeric; quantidade_itens bigint;
BEGIN
 SELECT valor_total INTO informado FROM pedidos WHERE id=alvo FOR UPDATE;
 IF NOT FOUND THEN RETURN; END IF;
 SELECT coalesce(sum(subtotal),0),count(*) INTO calculado,quantidade_itens FROM itens_pedido WHERE pedido_id=alvo;
 IF quantidade_itens<1 OR quantidade_itens>50 OR calculado>10000 OR informado<>calculado THEN
   RAISE EXCEPTION 'Confira os itens e o total do pedido. O limite é R$ 10.000,00 e 50 itens.'
     USING ERRCODE='23514', CONSTRAINT='ck_pedido_total_itens';
 END IF;
END $$;
CREATE OR REPLACE FUNCTION validar_total_pedido() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
 IF TG_TABLE_NAME='pedidos' THEN
   PERFORM conferir_total_pedido(NEW.id);
 ELSE
   IF TG_OP<>'INSERT' THEN PERFORM conferir_total_pedido(OLD.pedido_id); END IF;
   IF TG_OP<>'DELETE' THEN PERFORM conferir_total_pedido(NEW.pedido_id); END IF;
 END IF;
 RETURN NULL;
END $$;
DROP TRIGGER IF EXISTS ck_pedido_total_itens ON pedidos;
CREATE CONSTRAINT TRIGGER ck_pedido_total_itens AFTER INSERT OR UPDATE ON pedidos
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION validar_total_pedido();
DROP TRIGGER IF EXISTS ck_pedido_total_itens ON itens_pedido;
CREATE CONSTRAINT TRIGGER ck_pedido_total_itens AFTER INSERT OR UPDATE OR DELETE ON itens_pedido
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION validar_total_pedido();
COMMIT;

-- Resultado informativo: classifique estes adicionais pela interface atualizada.
SELECT id,nome FROM adicionais WHERE deleted_at IS NULL AND categoria IS NULL ORDER BY nome;
