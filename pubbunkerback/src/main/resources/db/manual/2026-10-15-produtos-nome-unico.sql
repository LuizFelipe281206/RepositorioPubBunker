BEGIN;

CREATE UNIQUE INDEX IF NOT EXISTS uk_produto_nome_nao_excluido
    ON produto (
    lower(btrim(regexp_replace(nome, '[[:space:]]+', ' ', 'g')))
    )
    WHERE deleted_at IS NULL;

COMMIT;