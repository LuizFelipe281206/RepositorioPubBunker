BEGIN;

ALTER TABLE comandas
    ADD COLUMN IF NOT EXISTS codigo_qr VARCHAR(36);

UPDATE comandas
SET codigo_qr = codigo_acesso
WHERE codigo_qr IS NULL;

ALTER TABLE comandas
    ALTER COLUMN codigo_qr SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_comanda_codigo_qr'
          AND conrelid = 'comandas'::regclass
    ) THEN
ALTER TABLE comandas
    ADD CONSTRAINT uk_comanda_codigo_qr
        UNIQUE (codigo_qr);
END IF;
END;
$$;

COMMIT;