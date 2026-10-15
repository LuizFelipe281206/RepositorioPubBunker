BEGIN;

ALTER TABLE itens_pedido
    ADD COLUMN IF NOT EXISTS produto_nome VARCHAR(255);
UPDATE itens_pedido AS item
SET produto_nome = produto.nome
    FROM produto
WHERE item.produto_id = produto.id
  AND item.produto_nome IS NULL;

ALTER TABLE itens_pedido
    ALTER COLUMN produto_nome SET NOT NULL;

COMMIT;
