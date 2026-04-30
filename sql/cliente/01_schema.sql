CREATE TABLE IF NOT EXISTS persona (
    persona_id      UUID         NOT NULL DEFAULT gen_random_uuid(),
    nombre          VARCHAR(100) NOT NULL,
    genero          VARCHAR(20),
    edad            INTEGER,
    tipo_documento  VARCHAR(20)  NOT NULL,
    numero_documento VARCHAR(50)  NOT NULL,
    direccion       VARCHAR(150),
    telefono        VARCHAR(20),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_persona PRIMARY KEY (persona_id),
    CONSTRAINT uq_persona_documento UNIQUE (tipo_documento, numero_documento)
);

CREATE TABLE IF NOT EXISTS cliente (
    persona_id     UUID         NOT NULL,
    contrasena     VARCHAR(255) NOT NULL,
    estado         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_cliente PRIMARY KEY (persona_id),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (persona_id)
        REFERENCES persona (persona_id)
);
