CREATE TABLE IF NOT EXISTS cliente_snapshot (
    cliente_id   UUID         NOT NULL,
    nombre       VARCHAR(100) NOT NULL,
    last_updated TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cliente_snapshot PRIMARY KEY (cliente_id)
);

CREATE TABLE IF NOT EXISTS cuenta (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    numero_cuenta    VARCHAR(20)  NOT NULL,
    tipo_cuenta      VARCHAR(20)  NOT NULL,
    saldo_inicial    NUMERIC(15,2) NOT NULL DEFAULT 0,
    saldo_disponible NUMERIC(15,2) NOT NULL DEFAULT 0,
    estado           BOOLEAN      NOT NULL DEFAULT TRUE,
    cliente_id       UUID         NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cuenta PRIMARY KEY (id),
    CONSTRAINT uq_cuenta_numero UNIQUE (numero_cuenta)
);

CREATE TABLE IF NOT EXISTS movimiento (
    id               UUID          NOT NULL DEFAULT gen_random_uuid(),
    fecha            TIMESTAMP     NOT NULL DEFAULT NOW(),
    tipo_movimiento  VARCHAR(20)   NOT NULL,
    valor            NUMERIC(15,2) NOT NULL,
    saldo            NUMERIC(15,2) NOT NULL,
    cuenta_id        UUID          NOT NULL,
    CONSTRAINT pk_movimiento PRIMARY KEY (id),
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (cuenta_id)
        REFERENCES cuenta (id)
);
