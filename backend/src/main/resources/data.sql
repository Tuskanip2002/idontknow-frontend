-- Categorías base para la app
INSERT INTO categorias (nombre) VALUES ('POLITICA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO categorias (nombre) VALUES ('ECONOMIA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO categorias (nombre) VALUES ('DEPORTES') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO categorias (nombre) VALUES ('TECNOLOGIA') ON CONFLICT (nombre) DO NOTHING;

