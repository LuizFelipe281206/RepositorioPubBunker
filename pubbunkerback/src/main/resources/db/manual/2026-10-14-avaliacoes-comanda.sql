BEGIN;

ALTER TABLE avaliacoes
    ALTER COLUMN usuario_id DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS comanda_id BIGINT
        REFERENCES comandas(id),
    ADD COLUMN IF NOT EXISTS data_abertura_atendimento TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS nome_cliente VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_avaliacoes_comanda_atendimento
    ON avaliacoes (
    comanda_id,
    data_abertura_atendimento,
    data_hora DESC
    )
    WHERE deleted_at IS NULL;

COMMIT;