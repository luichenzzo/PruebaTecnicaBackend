-- Reset schema so this script can be re-run safely during local development.
DROP TABLE IF EXISTS
    transfer_items,
    transfers,
    sale_items,
    sales,
    purchase_order_items,
    purchase_orders,
    inventory_movements,
    inventories,
    users,
    products,
    suppliers,
    unit_of_measure,
    branches
    CASCADE;

DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;

DROP TYPE IF EXISTS transfer_status_enum CASCADE;
DROP TYPE IF EXISTS sale_status_enum CASCADE;
DROP TYPE IF EXISTS order_status_enum CASCADE;
DROP TYPE IF EXISTS movement_type_enum CASCADE;
DROP TYPE IF EXISTS role_enum CASCADE;

-- Enable pgcrypto for gen_random_uuid(); change if you prefer uuid-ossp/uuid_generate_v4()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- NOTE: enums were previously used here. We persist enums as strings (text) in the DB
-- to avoid PostgreSQL enum binding issues from JDBC. If you want to re-enable
-- native PG enums, recreate the types and adjust the entity mappings accordingly.

-- Generic function to auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- BRANCHES
CREATE TABLE branches (
                          id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                          created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                          updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                          created_by_id uuid,
                          updated_by_id uuid,
                          code text NOT NULL UNIQUE,
                          name text NOT NULL,
                          address text
);

-- UNIT OF MEASURE
CREATE TABLE unit_of_measure (
                                 id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                 created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                 updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                 created_by_id uuid,
                                 updated_by_id uuid,
                                 code text NOT NULL UNIQUE,
                                 name text NOT NULL,
                                 description text
);

-- SUPPLIERS
CREATE TABLE suppliers (
                           id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                           created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                           updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                           created_by_id uuid,
                           updated_by_id uuid,
                           name text NOT NULL,
                           contact_name text,
                           contact_email text,
                           phone text
);

-- PRODUCTS
CREATE TABLE products (
                          id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                          created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                          updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                          created_by_id uuid,
                          updated_by_id uuid,
                          sku text NOT NULL UNIQUE,
                          name text NOT NULL,
                          description text,
                          unit_of_measure_id uuid REFERENCES unit_of_measure(id) ON DELETE SET NULL,
                          supplier_id uuid REFERENCES suppliers(id) ON DELETE SET NULL,
                          default_cost numeric(19,4)
);

CREATE INDEX idx_products_unit_of_measure_id ON products(unit_of_measure_id);
CREATE INDEX idx_products_supplier_id ON products(supplier_id);

-- USERS
CREATE TABLE users (
                       id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                       created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                       updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                       created_by_id uuid,
                       updated_by_id uuid,
                       username text NOT NULL UNIQUE,
                       full_name text NOT NULL,
                       email text NOT NULL UNIQUE,
                       password_hash text,
                       role text NOT NULL,
                       branch_id uuid REFERENCES branches(id) ON DELETE SET NULL
);

CREATE INDEX idx_users_branch_id ON users(branch_id);

-- INVENTORIES (one inventory row per product per branch) with uniqueness constraint
CREATE TABLE inventories (
                             id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                             created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                             updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                             created_by_id uuid,
                             updated_by_id uuid,
                             product_id uuid NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                             branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                             quantity numeric(19,4) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
                             reserved numeric(19,4) NOT NULL DEFAULT 0 CHECK (reserved >= 0),
                             CONSTRAINT uq_inventories_product_branch UNIQUE (product_id, branch_id)
);

CREATE INDEX idx_inventories_product_id ON inventories(product_id);
CREATE INDEX idx_inventories_branch_id ON inventories(branch_id);

-- INVENTORY MOVEMENTS (auditable)
CREATE TABLE inventory_movements (
                                     id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                     created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                     updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                     inventory_id uuid NOT NULL REFERENCES inventories(id) ON DELETE CASCADE,
                                     movement_type text NOT NULL,
                                     quantity numeric(19,4) NOT NULL CHECK (quantity >= 0),
                                     reference text,
                                     notes text,
                                     source_type text,
                                     source_id text,
                                     created_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                                     updated_by_id uuid REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_inventory_movements_inventory_id ON inventory_movements(inventory_id);
CREATE INDEX idx_inventory_movements_created_by_id ON inventory_movements(created_by_id);

-- PURCHASE ORDERS
CREATE TABLE purchase_orders (
                                 id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                 created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                 updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                 order_number text NOT NULL UNIQUE,
                                 supplier_id uuid NOT NULL REFERENCES suppliers(id) ON DELETE RESTRICT,
                                 branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                                 status text NOT NULL DEFAULT 'PENDING',
                                 created_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                                 updated_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                                 total numeric(19,4)
);

CREATE INDEX idx_purchase_orders_supplier_id ON purchase_orders(supplier_id);
CREATE INDEX idx_purchase_orders_branch_id ON purchase_orders(branch_id);
CREATE INDEX idx_purchase_orders_created_by_id ON purchase_orders(created_by_id);

-- PURCHASE ORDER ITEMS (owned by purchase_orders; cascade on delete)
CREATE TABLE purchase_order_items (
                                      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                      created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                      updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                      created_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                                      updated_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                                      purchase_order_id uuid NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
                                      product_id uuid NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                                      quantity numeric(19,4) NOT NULL CHECK (quantity >= 0),
                                      unit_price numeric(19,4)
);

CREATE INDEX idx_purchase_order_items_purchase_order_id ON purchase_order_items(purchase_order_id);
CREATE INDEX idx_purchase_order_items_product_id ON purchase_order_items(product_id);

-- SALES
CREATE TABLE sales (
                       id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                       created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                       updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                       sale_number text NOT NULL UNIQUE,
                       branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                       status text NOT NULL DEFAULT 'PENDING',
                       created_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                       updated_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                       total numeric(19,4)
);

CREATE INDEX idx_sales_branch_id ON sales(branch_id);
CREATE INDEX idx_sales_created_by_id ON sales(created_by_id);

-- SALE ITEMS (owned by sales; cascade on delete)
CREATE TABLE sale_items (
                            id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                            created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                            updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                            created_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                            updated_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                            sale_id uuid NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
                            product_id uuid NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                            quantity numeric(19,4) NOT NULL CHECK (quantity >= 0),
                            unit_price numeric(19,4)
);

CREATE INDEX idx_sale_items_sale_id ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items(product_id);

-- TRANSFERS (inter-branch)
CREATE TABLE transfers (
                           id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                           created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                           updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                           transfer_number text NOT NULL UNIQUE,
                           from_branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                           to_branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
                           status text NOT NULL DEFAULT 'PENDING',
                           created_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                           updated_by_id uuid REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_transfers_from_branch_id ON transfers(from_branch_id);
CREATE INDEX idx_transfers_to_branch_id ON transfers(to_branch_id);
CREATE INDEX idx_transfers_created_by_id ON transfers(created_by_id);

-- TRANSFER ITEMS (owned by transfers; cascade on delete)
CREATE TABLE transfer_items (
                                id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                created_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                updated_at timestamp WITHOUT time zone NOT NULL DEFAULT now(),
                                created_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                                updated_by_id uuid REFERENCES users(id) ON DELETE SET NULL,
                                transfer_id uuid NOT NULL REFERENCES transfers(id) ON DELETE CASCADE,
                                product_id uuid NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                                quantity numeric(19,4) NOT NULL CHECK (quantity >= 0)
);

CREATE INDEX idx_transfer_items_transfer_id ON transfer_items(transfer_id);
CREATE INDEX idx_transfer_items_product_id ON transfer_items(product_id);

-- Attach update_updated_at trigger to tables that have updated_at
DO $$
BEGIN
  -- List of tables to attach the trigger to
  PERFORM 1 FROM pg_catalog.pg_tables WHERE schemaname = 'public' AND tablename = 'branches';
  IF FOUND THEN
    EXECUTE 'CREATE TRIGGER trg_branches_updated_at BEFORE UPDATE ON branches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
END IF;

EXECUTE 'CREATE TRIGGER trg_unit_of_measure_updated_at BEFORE UPDATE ON unit_of_measure FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_suppliers_updated_at BEFORE UPDATE ON suppliers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_inventories_updated_at BEFORE UPDATE ON inventories FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_inventory_movements_updated_at BEFORE UPDATE ON inventory_movements FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_purchase_orders_updated_at BEFORE UPDATE ON purchase_orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_purchase_order_items_updated_at BEFORE UPDATE ON purchase_order_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_sales_updated_at BEFORE UPDATE ON sales FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_sale_items_updated_at BEFORE UPDATE ON sale_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_transfers_updated_at BEFORE UPDATE ON transfers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
EXECUTE 'CREATE TRIGGER trg_transfer_items_updated_at BEFORE UPDATE ON transfer_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();';
END;
$$ LANGUAGE plpgsql;

-- Optional: add comments for clarity
COMMENT ON TABLE inventories IS 'Inventory record per product per branch — unique(product, branch). Quantities stored here along with reserved stock.';
COMMENT ON TABLE inventory_movements IS 'Auditable inventory movement records; link to user who created the movement.';
-- Seed data
-- Login credentials:
-- ADMIN:    admin_root / admin123
-- MANAGER:  manager_north / manager123
-- MANAGER:  manager_south / manager123
-- OPERATOR: operator_store1 / operator123
-- OPERATOR: operator_store2 / demo123

INSERT INTO branches (id, created_at, updated_at, created_by_id, updated_by_id, code, name, address) VALUES
                                                                                                         ('10000000-0000-0000-0000-000000000001', '2026-05-01 08:00:00', '2026-05-01 08:00:00', NULL, NULL, 'BOG-01', 'Bogota Centro', 'Cra 7 #12-34, Bogota'),
                                                                                                         ('10000000-0000-0000-0000-000000000002', '2026-05-01 08:05:00', '2026-05-01 08:05:00', NULL, NULL, 'BOG-02', 'Bogota Norte', 'Calle 100 #15-20, Bogota'),
                                                                                                         ('10000000-0000-0000-0000-000000000003', '2026-05-01 08:10:00', '2026-05-01 08:10:00', NULL, NULL, 'MED-01', 'Medellin Centro', 'Av Oriental #45-18, Medellin'),
                                                                                                         ('10000000-0000-0000-0000-000000000004', '2026-05-01 08:15:00', '2026-05-01 08:15:00', NULL, NULL, 'CAL-01', 'Cali Sur', 'Carrera 80 #10-55, Cali'),
                                                                                                         ('10000000-0000-0000-0000-000000000005', '2026-05-01 08:20:00', '2026-05-01 08:20:00', NULL, NULL, 'BAR-01', 'Barranquilla Norte', 'Via 40 #72-10, Barranquilla');

INSERT INTO unit_of_measure (id, created_at, updated_at, created_by_id, updated_by_id, code, name, description) VALUES
                                                                                                                    ('20000000-0000-0000-0000-000000000001', '2026-05-01 08:30:00', '2026-05-01 08:30:00', NULL, NULL, 'UN', 'Unidad', 'Unidad individual'),
                                                                                                                    ('20000000-0000-0000-0000-000000000002', '2026-05-01 08:31:00', '2026-05-01 08:31:00', NULL, NULL, 'KG', 'Kilogramo', 'Peso en kilogramos'),
                                                                                                                    ('20000000-0000-0000-0000-000000000003', '2026-05-01 08:32:00', '2026-05-01 08:32:00', NULL, NULL, 'LT', 'Litro', 'Volumen en litros'),
                                                                                                                    ('20000000-0000-0000-0000-000000000004', '2026-05-01 08:33:00', '2026-05-01 08:33:00', NULL, NULL, 'CJ', 'Caja', 'Caja cerrada'),
                                                                                                                    ('20000000-0000-0000-0000-000000000005', '2026-05-01 08:34:00', '2026-05-01 08:34:00', NULL, NULL, 'PQ', 'Paquete', 'Paquete comercial');

INSERT INTO suppliers (id, created_at, updated_at, created_by_id, updated_by_id, name, contact_name, contact_email, phone) VALUES
                                                                                                                               ('30000000-0000-0000-0000-000000000001', '2026-05-01 08:40:00', '2026-05-01 08:40:00', NULL, NULL, 'Andes Supply SAS', 'Laura Mejia', 'laura@andes-supply.com', '3001111111'),
                                                                                                                               ('30000000-0000-0000-0000-000000000002', '2026-05-01 08:41:00', '2026-05-01 08:41:00', NULL, NULL, 'Logistica Caribe', 'Mario Perez', 'mario@caribe-logistica.com', '3002222222'),
                                                                                                                               ('30000000-0000-0000-0000-000000000003', '2026-05-01 08:42:00', '2026-05-01 08:42:00', NULL, NULL, 'Cafe Industrial', 'Sara Londono', 'sara@cafeindustrial.com', '3003333333'),
                                                                                                                               ('30000000-0000-0000-0000-000000000004', '2026-05-01 08:43:00', '2026-05-01 08:43:00', NULL, NULL, 'Empaques del Valle', 'Juan Ruiz', 'juan@empaquesdelvalle.com', '3004444444'),
                                                                                                                               ('30000000-0000-0000-0000-000000000005', '2026-05-01 08:44:00', '2026-05-01 08:44:00', NULL, NULL, 'Distribuciones Pacifico', 'Ana Torres', 'ana@distribucionespacifico.com', '3005555555');

INSERT INTO products (id, created_at, updated_at, created_by_id, updated_by_id, sku, name, description, unit_of_measure_id, supplier_id, default_cost) VALUES
                                                                                                                                                           ('40000000-0000-0000-0000-000000000001', '2026-05-01 09:00:00', '2026-05-01 09:00:00', NULL, NULL, 'SKU-CAF-001', 'Cafe Molido Premium', 'Bolsa de cafe molido premium 500g', '20000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000003', 8.0000),
                                                                                                                                                           ('40000000-0000-0000-0000-000000000002', '2026-05-01 09:01:00', '2026-05-01 09:01:00', NULL, NULL, 'SKU-AZU-002', 'Azucar Morena', 'Paquete de azucar morena 1kg', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 15.0000),
                                                                                                                                                           ('40000000-0000-0000-0000-000000000003', '2026-05-01 09:02:00', '2026-05-01 09:02:00', NULL, NULL, 'SKU-LEC-003', 'Leche Entera', 'Leche entera en presentacion 1 litro', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', 12.0000),
                                                                                                                                                           ('40000000-0000-0000-0000-000000000004', '2026-05-01 09:03:00', '2026-05-01 09:03:00', NULL, NULL, 'SKU-EMP-004', 'Caja de Empaque', 'Caja de carton para despacho', '20000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000004', 5.0000),
                                                                                                                                                           ('40000000-0000-0000-0000-000000000005', '2026-05-01 09:04:00', '2026-05-01 09:04:00', NULL, NULL, 'SKU-TES-005', 'Te Verde', 'Caja de te verde x 20 sobres', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000005', 20.0000);

INSERT INTO users (id, created_at, updated_at, created_by_id, updated_by_id, username, full_name, email, password_hash, role, branch_id) VALUES
                                                                                                                                             ('50000000-0000-0000-0000-000000000001', '2026-05-01 09:30:00', '2026-05-01 09:30:00', NULL, NULL, 'admin_root', 'Admin Root', 'admin@optiplant.local', '$2a$10$o.T/ujd3Z9deeAXmWZPcLeosPqjWnRofkauZN/WdIAyotWZH.6OHy', 'ADMIN', '10000000-0000-0000-0000-000000000001'),
                                                                                                                                             ('50000000-0000-0000-0000-000000000002', '2026-05-01 09:31:00', '2026-05-01 09:31:00', '50000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'manager_north', 'Manager North', 'manager.north@optiplant.local', '$2a$10$IxIBsPGWcMBtdkwMUuN7I.r/4KDsUrsHcmfN6gZVlft8a4qSWA1ia', 'MANAGER', '10000000-0000-0000-0000-000000000001'),
                                                                                                                                             ('50000000-0000-0000-0000-000000000003', '2026-05-01 09:32:00', '2026-05-01 09:32:00', '50000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'manager_south', 'Manager South', 'manager.south@optiplant.local', '$2a$10$IxIBsPGWcMBtdkwMUuN7I.r/4KDsUrsHcmfN6gZVlft8a4qSWA1ia', 'MANAGER', '10000000-0000-0000-0000-000000000003'),
                                                                                                                                             ('50000000-0000-0000-0000-000000000004', '2026-05-01 09:33:00', '2026-05-01 09:33:00', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 'operator_store1', 'Operator Store 1', 'operator1@optiplant.local', '$2a$10$w8n9hegGHVehFQXB7EgvxOjNPV9V/fNyB5btYoj.u8YT/D81nOicW', 'OPERATOR', '10000000-0000-0000-0000-000000000001'),
                                                                                                                                             ('50000000-0000-0000-0000-000000000005', '2026-05-01 09:34:00', '2026-05-01 09:34:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'operator_store2', 'Operator Store 2', 'operator2@optiplant.local', '$2a$10$Y31KUtSir.EXS7RXlWt55egsOWzK2kKSoTZdZl1tm0vm7mqQDLQz6', 'OPERATOR', '10000000-0000-0000-0000-000000000003');

INSERT INTO inventories (id, created_at, updated_at, created_by_id, updated_by_id, product_id, branch_id, quantity, reserved) VALUES
                                                                                                                                  ('60000000-0000-0000-0000-000000000001', '2026-05-01 10:00:00', '2026-05-03 16:00:00', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 95.0000, 0.0000),
                                                                                                                                  ('60000000-0000-0000-0000-000000000002', '2026-05-01 10:01:00', '2026-05-03 16:00:00', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 10.0000, 0.0000),
                                                                                                                                  ('60000000-0000-0000-0000-000000000003', '2026-05-01 10:02:00', '2026-05-03 15:00:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 47.0000, 5.0000),
                                                                                                                                  ('60000000-0000-0000-0000-000000000004', '2026-05-01 10:03:00', '2026-05-03 15:30:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', 8.0000, 0.0000),
                                                                                                                                  ('60000000-0000-0000-0000-000000000005', '2026-05-01 10:04:00', '2026-05-03 15:45:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', 12.0000, 2.0000);

INSERT INTO purchase_orders (id, created_at, updated_at, order_number, supplier_id, branch_id, status, created_by_id, updated_by_id, total) VALUES
                                                                                                                                                ('80000000-0000-0000-0000-000000000001', '2026-05-02 08:00:00', '2026-05-02 12:00:00', 'PO-20260502-001', '30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 'RECEIVED', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 960.0000),
                                                                                                                                                ('80000000-0000-0000-0000-000000000002', '2026-05-02 08:30:00', '2026-05-02 13:00:00', 'PO-20260502-002', '30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'RECEIVED', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 750.0000),
                                                                                                                                                ('80000000-0000-0000-0000-000000000003', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 'PO-20260502-003', '30000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', 'PENDING', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 600.0000),
                                                                                                                                                ('80000000-0000-0000-0000-000000000004', '2026-05-02 09:30:00', '2026-05-02 10:15:00', 'PO-20260502-004', '30000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', 'CANCELLED', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 50.0000),
                                                                                                                                                ('80000000-0000-0000-0000-000000000005', '2026-05-02 10:00:00', '2026-05-02 10:00:00', 'PO-20260502-005', '30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003', 'PENDING', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 840.0000);

INSERT INTO purchase_order_items (id, created_at, updated_at, created_by_id, updated_by_id, purchase_order_id, product_id, quantity, unit_price) VALUES
                                                                                                                                                     ('81000000-0000-0000-0000-000000000001', '2026-05-02 08:00:00', '2026-05-02 12:00:00', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 120.0000, 8.0000),
                                                                                                                                                     ('81000000-0000-0000-0000-000000000002', '2026-05-02 08:30:00', '2026-05-02 13:00:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', '80000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 50.0000, 15.0000),
                                                                                                                                                     ('81000000-0000-0000-0000-000000000003', '2026-05-02 09:00:00', '2026-05-02 09:00:00', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000005', 30.0000, 20.0000),
                                                                                                                                                     ('81000000-0000-0000-0000-000000000004', '2026-05-02 09:30:00', '2026-05-02 10:15:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', '80000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', 10.0000, 5.0000),
                                                                                                                                                     ('81000000-0000-0000-0000-000000000005', '2026-05-02 10:00:00', '2026-05-02 10:00:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', '80000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000003', 70.0000, 12.0000);

INSERT INTO sales (id, created_at, updated_at, sale_number, branch_id, status, created_by_id, updated_by_id, total) VALUES
                                                                                                                        ('90000000-0000-0000-0000-000000000001', '2026-05-03 09:00:00', '2026-05-03 09:05:00', 'SALE-20260503-001', '10000000-0000-0000-0000-000000000001', 'COMPLETED', '50000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', 270.0000),
                                                                                                                        ('90000000-0000-0000-0000-000000000002', '2026-05-03 09:10:00', '2026-05-03 09:10:00', 'SALE-20260503-002', '10000000-0000-0000-0000-000000000001', 'PENDING', '50000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', 250.0000),
                                                                                                                        ('90000000-0000-0000-0000-000000000003', '2026-05-03 09:20:00', '2026-05-03 09:25:00', 'SALE-20260503-003', '10000000-0000-0000-0000-000000000004', 'CANCELLED', '50000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005', 60.0000),
                                                                                                                        ('90000000-0000-0000-0000-000000000004', '2026-05-03 09:30:00', '2026-05-03 09:30:00', 'SALE-20260503-004', '10000000-0000-0000-0000-000000000003', 'PENDING', '50000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005', 110.0000),
                                                                                                                        ('90000000-0000-0000-0000-000000000005', '2026-05-03 09:40:00', '2026-05-03 09:40:00', 'SALE-20260503-005', '10000000-0000-0000-0000-000000000005', 'PENDING', '50000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', 30.0000);

INSERT INTO sale_items (id, created_at, updated_at, created_by_id, updated_by_id, sale_id, product_id, quantity, unit_price) VALUES
                                                                                                                                 ('91000000-0000-0000-0000-000000000001', '2026-05-03 09:00:00', '2026-05-03 09:05:00', '50000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', '90000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 15.0000, 18.0000),
                                                                                                                                 ('91000000-0000-0000-0000-000000000002', '2026-05-03 09:10:00', '2026-05-03 09:10:00', '50000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', '90000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 10.0000, 25.0000),
                                                                                                                                 ('91000000-0000-0000-0000-000000000003', '2026-05-03 09:20:00', '2026-05-03 09:25:00', '50000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005', '90000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000004', 6.0000, 10.0000),
                                                                                                                                 ('91000000-0000-0000-0000-000000000004', '2026-05-03 09:30:00', '2026-05-03 09:30:00', '50000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005', '90000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000003', 5.0000, 22.0000),
                                                                                                                                 ('91000000-0000-0000-0000-000000000005', '2026-05-03 09:40:00', '2026-05-03 09:40:00', '50000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', '90000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000005', 1.0000, 30.0000);

INSERT INTO transfers (id, created_at, updated_at, transfer_number, from_branch_id, to_branch_id, status, created_by_id, updated_by_id) VALUES
                                                                                                                                            ('a0000000-0000-0000-0000-000000000001', '2026-05-03 11:00:00', '2026-05-03 12:00:00', 'TRF-20260503-001', '10000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'COMPLETED', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002'),
                                                                                                                                            ('a0000000-0000-0000-0000-000000000002', '2026-05-03 11:10:00', '2026-05-03 11:25:00', 'TRF-20260503-002', '10000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'IN_TRANSIT', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002'),
                                                                                                                                            ('a0000000-0000-0000-0000-000000000003', '2026-05-03 11:20:00', '2026-05-03 11:20:00', 'TRF-20260503-003', '10000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', 'PENDING', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003'),
                                                                                                                                            ('a0000000-0000-0000-0000-000000000004', '2026-05-03 11:30:00', '2026-05-03 11:35:00', 'TRF-20260503-004', '10000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003', 'CANCELLED', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003'),
                                                                                                                                            ('a0000000-0000-0000-0000-000000000005', '2026-05-03 11:40:00', '2026-05-03 11:40:00', 'TRF-20260503-005', '10000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004', 'PENDING', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003');

INSERT INTO transfer_items (id, created_at, updated_at, created_by_id, updated_by_id, transfer_id, product_id, quantity) VALUES
                                                                                                                             ('b0000000-0000-0000-0000-000000000001', '2026-05-03 11:00:00', '2026-05-03 12:00:00', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 10.0000),
                                                                                                                             ('b0000000-0000-0000-0000-000000000002', '2026-05-03 11:10:00', '2026-05-03 11:10:00', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 3.0000),
                                                                                                                             ('b0000000-0000-0000-0000-000000000003', '2026-05-03 11:20:00', '2026-05-03 11:20:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000005', 4.0000),
                                                                                                                             ('b0000000-0000-0000-0000-000000000004', '2026-05-03 11:30:00', '2026-05-03 11:35:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', 2.0000),
                                                                                                                             ('b0000000-0000-0000-0000-000000000005', '2026-05-03 11:40:00', '2026-05-03 11:40:00', '50000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000003', 5.0000);

INSERT INTO inventory_movements (id, created_at, updated_at, inventory_id, movement_type, quantity, reference, notes, source_type, source_id, created_by_id, updated_by_id) VALUES
                                                                                                                                                                                ('70000000-0000-0000-0000-000000000001', '2026-05-02 12:00:00', '2026-05-02 12:00:00', '60000000-0000-0000-0000-000000000001', 'PURCHASE_IN', 120.0000, 'PO-20260502-001', 'Purchase order received for Bogota Centro', 'PURCHASE_ORDER', '80000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002'),
                                                                                                                                                                                ('70000000-0000-0000-0000-000000000002', '2026-05-03 11:25:00', '2026-05-03 11:25:00', '60000000-0000-0000-0000-000000000003', 'TRANSFER_OUT', 3.0000, 'TRF-20260503-002', 'Approved transfer from Bogota Centro to Barranquilla Norte', 'TRANSFER', 'a0000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002'),
                                                                                                                                                                                ('70000000-0000-0000-0000-000000000003', '2026-05-03 09:05:00', '2026-05-03 09:05:00', '60000000-0000-0000-0000-000000000001', 'SALE_OUT', 15.0000, 'SALE-20260503-001', 'Completed sale from Bogota Centro', 'SALE', '90000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004'),
                                                                                                                                                                                ('70000000-0000-0000-0000-000000000004', '2026-05-03 11:30:00', '2026-05-03 11:30:00', '60000000-0000-0000-0000-000000000001', 'TRANSFER_OUT', 10.0000, 'TRF-20260503-001', 'Approved transfer from Bogota Centro to Bogota Norte', 'TRANSFER', 'a0000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002'),
                                                                                                                                                                                ('70000000-0000-0000-0000-000000000005', '2026-05-03 12:00:00', '2026-05-03 12:00:00', '60000000-0000-0000-0000-000000000002', 'TRANSFER_IN', 10.0000, 'TRF-20260503-001', 'Completed transfer received in Bogota Norte', 'TRANSFER', 'a0000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002');
