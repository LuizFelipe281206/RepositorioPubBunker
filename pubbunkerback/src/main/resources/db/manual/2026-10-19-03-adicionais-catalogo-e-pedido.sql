-- Correção da regra: catálogo com vínculos ilimitados; até 4 adicionais por item do pedido.
-- Aplicar depois da revisão 02, com o backend parado. Não exclui produtos, vínculos ou pedidos.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '60s';

DROP TRIGGER IF EXISTS ck_produto_quatro_adicionais ON produto_adicionais;
DROP FUNCTION IF EXISTS validar_quatro_adicionais_produto();

CREATE OR REPLACE FUNCTION validar_quatro_adicionais_item_pedido() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE quantidade bigint;
BEGIN
 PERFORM 1 FROM itens_pedido WHERE id=NEW.item_pedido_id FOR UPDATE;
 IF TG_TABLE_NAME='item_pedido_adicionais_snapshot' THEN
   SELECT count(*) INTO quantidade FROM item_pedido_adicionais_snapshot
     WHERE item_pedido_id=NEW.item_pedido_id;
 ELSE
   SELECT count(*) INTO quantidade FROM item_pedido_adicionais
     WHERE item_pedido_id=NEW.item_pedido_id;
 END IF;
 IF quantidade>4 THEN
   RAISE EXCEPTION 'Selecione no máximo 4 adicionais por item do pedido.'
     USING ERRCODE='23514', CONSTRAINT='ck_item_pedido_quatro_adicionais';
 END IF;
 RETURN NULL;
END $$;

DROP TRIGGER IF EXISTS ck_item_pedido_quatro_adicionais ON item_pedido_adicionais;
CREATE CONSTRAINT TRIGGER ck_item_pedido_quatro_adicionais
 AFTER INSERT OR UPDATE ON item_pedido_adicionais DEFERRABLE INITIALLY DEFERRED
 FOR EACH ROW EXECUTE FUNCTION validar_quatro_adicionais_item_pedido();

DROP TRIGGER IF EXISTS ck_item_pedido_quatro_adicionais ON item_pedido_adicionais_snapshot;
CREATE CONSTRAINT TRIGGER ck_item_pedido_quatro_adicionais
 AFTER INSERT OR UPDATE ON item_pedido_adicionais_snapshot DEFERRABLE INITIALLY DEFERRED
 FOR EACH ROW EXECUTE FUNCTION validar_quatro_adicionais_item_pedido();
COMMIT;
