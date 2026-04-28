-- BaseDatos.sql
-- Schema para cliente-service (PostgreSQL 15)
-- Usa la extensión pgcrypto para gen_random_uuid()

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Tabla: persona
CREATE TABLE IF NOT EXISTS persona (
  persona_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre TEXT NOT NULL,
  genero TEXT,
  edad INTEGER,
  identificacion TEXT NOT NULL UNIQUE,
  direccion TEXT,
  telefono TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Tabla: cliente (JOINED inheritance desde persona)
CREATE TABLE IF NOT EXISTS cliente (
  persona_id UUID PRIMARY KEY,
  contrasena_hash TEXT NOT NULL,
  estado BOOLEAN DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  CONSTRAINT fk_cliente_persona FOREIGN KEY (persona_id) REFERENCES persona (persona_id)
);

-- Índice para consultas por estado (clientes activos/inactivos)
CREATE INDEX IF NOT EXISTS idx_cliente_estado ON cliente (estado);
