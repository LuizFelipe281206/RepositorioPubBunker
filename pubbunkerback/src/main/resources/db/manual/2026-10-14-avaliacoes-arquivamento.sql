BEGIN;

ALTER TABLE avaliacoes
    ADD COLUMN IF NOT EXISTS arquivado_em TIMESTAMP(6);

CREATE INDEX IF NOT EXISTS idx_avaliacoes_admin_ativas
    ON avaliacoes (data_hora DESC)
    WHERE deleted_at IS NULL AND arquivado_em IS NULL;

CREATE INDEX IF NOT EXISTS idx_avaliacoes_admin_arquivadas
    ON avaliacoes (data_hora DESC)
    WHERE deleted_at IS NULL AND arquivado_em IS NOT NULL;

COMMIT;