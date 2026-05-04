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

-- Enum types (mirror Java @Enumerated(EnumType.STRING))
CREATE TYPE role_enum AS ENUM ('ADMIN', 'MANAGER', 'OPERATOR');

CREATE TYPE movement_type_enum AS ENUM (
  'PURCHASE_IN',
  'SALE_OUT',
  'ADJUSTMENT',
  'TRANSFER_IN',
  'TRANSFER_OUT'
);

CREATE TYPE order_status_enum AS ENUM ('PENDING', 'RECEIVED', 'CANCELLED');

CREATE TYPE sale_status_enum AS ENUM ('PENDING', 'COMPLETED', 'CANCELLED');

CREATE TYPE transfer_status_enum AS ENUM ('PENDING', 'IN_TRANSIT', 'COMPLETED', 'CANCELLED');

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
                       role role_enum NOT NULL,
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
                                     movement_type movement_type_enum NOT NULL,
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
                                 status order_status_enum NOT NULL DEFAULT 'PENDING',
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
                       status sale_status_enum NOT NULL DEFAULT 'PENDING',
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
                           status transfer_status_enum NOT NULL DEFAULT 'PENDING',
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
