DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS plots           CASCADE;
DROP TABLE IF EXISTS employees       CASCADE;
DROP TABLE IF EXISTS sectors         CASCADE;
DROP TABLE IF EXISTS customers       CASCADE;

DROP TYPE IF EXISTS plot_status   CASCADE;
DROP TYPE IF EXISTS request_status CASCADE;
DROP TYPE IF EXISTS employee_role  CASCADE;

-- 1. Создание перечислений (ENUM) для статусов и ролей
CREATE TYPE plot_status AS ENUM ('FREE', 'RESERVED', 'OCCUPIED');
CREATE TYPE request_status AS ENUM ('NEW', 'PROCESSING', 'APPROVED', 'REJECTED', 'COMPLETED');
CREATE TYPE employee_role AS ENUM ('ADMIN', 'MANAGER', 'WORKER');

-- 2. Таблица: Заявители (Клиенты)
CREATE TABLE IF NOT EXISTS customers (
                           id BIGSERIAL PRIMARY KEY,
                           full_name VARCHAR(150) NOT NULL,
                           phone VARCHAR(20) NOT NULL,
                           email VARCHAR(100),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO customers(full_name, phone) VALUES ('Поздняков Владислав Оуджибудов', '+71111111111');
INSERT INTO customers(full_name, phone) VALUES ('Халяпин Алексей Оуджибудов', '+72222222222');
INSERT INTO customers(full_name, phone) VALUES ('Степан Степанов Оуджибудов', '+733333333333');
INSERT INTO customers(full_name, phone) VALUES ('Костик Админ Оуджибудов', '+74444444444');
INSERT INTO customers(full_name, phone) VALUES ('Воло Ботанов Оуджибудов', '+75555555555');
INSERT INTO customers(full_name, phone) VALUES ('Владислав Кураков Оуджибудов', '+76666666666');
INSERT INTO customers(full_name, phone) VALUES ('Никита Киков Оуджибудов', '+77777777777');

-- 3. Таблица: Сектора кладбища
CREATE TABLE IF NOT EXISTS sectors (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sectors (name) VALUES ('Весенний');
INSERT INTO sectors (name) VALUES ('Летний');
INSERT INTO sectors (name) VALUES ('Зимний');
INSERT INTO sectors (name) VALUES ('Осеннний');
INSERT INTO sectors (name) VALUES ('Арзамас');
INSERT INTO sectors (name) VALUES ('Бердск');

-- 4. Таблица: Места захоронения
CREATE TABLE IF NOT EXISTS plots (
                       id BIGSERIAL PRIMARY KEY,
                       sector_id BIGINT NOT NULL REFERENCES sectors(id) ON DELETE RESTRICT,
                       row_number INT NOT NULL,
                       plot_number INT NOT NULL,
                       status plot_status DEFAULT 'FREE',
                       length_cm INT DEFAULT 200,     -- Стандартная длина
                       width_cm INT DEFAULT 100,      -- Стандартная ширина
                       coordinates VARCHAR(100),      -- GPS (например, "55.7558, 37.6173")
                       UNIQUE (sector_id, row_number, plot_number) -- Защита от дублей мест
);

INSERT INTO plots(sector_id, row_number, plot_number, coordinates, status) VALUES (1, 2, 1, '55.7558, 37.6173', 'RESERVED');
INSERT INTO plots(sector_id, row_number, plot_number, coordinates, status) VALUES (2, 1, 1, '28.124, 12.213', 'OCCUPIED');
INSERT INTO plots(sector_id, row_number, plot_number, coordinates, status) VALUES (1, 1, 1, '69.6767, 67.6969', 'FREE');
INSERT INTO plots(sector_id, row_number, plot_number, coordinates, status) VALUES (3, 2, 1, '1.0, 2.0', 'FREE');
INSERT INTO plots(sector_id, row_number, plot_number, coordinates) VALUES (4, 1, 1, '12.12, 33.222');
INSERT INTO plots(sector_id, row_number, plot_number, coordinates, status) VALUES (5, 1, 1, '99.41234, 66.125', 'OCCUPIED');

-- 5. Таблица: Сотрудники
CREATE TABLE IF NOT EXISTS employees (
                           id BIGSERIAL PRIMARY KEY,
                           full_name VARCHAR(150) NOT NULL,
                           role employee_role DEFAULT 'MANAGER',
                           login VARCHAR(50) UNIQUE NOT NULL,
                           password_hash VARCHAR(255) NOT NULL,
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO employees(full_name, login, password_hash) VALUES ('Гой Гоевич Гоев', 'goy@mail.mail', 'password');
INSERT INTO employees(full_name, login, password_hash) VALUES ('Наш Слон Слонович', 'elephant@mail.mail', 'password');
INSERT INTO employees(full_name, login, password_hash) VALUES ('Полка Полка Полка', 'polka@mail.mail', 'password');
INSERT INTO employees(full_name, login, password_hash) VALUES ('Стул Стул Стулевич', 'chair@mail.mail', 'password');
INSERT INTO employees(full_name, login, password_hash) VALUES ('Телефон Телефонов Телефонович', 'phone@mail.mail', 'password');
INSERT INTO employees(full_name, login, password_hash) VALUES ('Кактотам Будейко Какойтотам', 'budeiko@mail.mail', 'password');

-- 6. Таблица: Заявки (содержит данные об умершем)
CREATE TABLE IF NOT EXISTS requests (
                                 id BIGSERIAL PRIMARY KEY,

    -- Внешние ключи
                                 customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
                                 plot_id BIGINT NOT NULL REFERENCES plots(id) ON DELETE RESTRICT,
                                 employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,

    -- Данные умершего (без NOT NULL для возможности прижизненной брони)
                                 deceased_full_name VARCHAR(150),
                                 deceased_birth_date DATE,
                                 deceased_death_date DATE,
                                 deceased_certificate VARCHAR(100),

    -- Метаданные заявки
                                 status request_status DEFAULT 'NEW',
                                 request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 processed_date TIMESTAMP,
                                 total_cost DECIMAL(10, 2) DEFAULT 0.00,
                                 notes TEXT,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO requests(customer_id, plot_id, employee_id, processed_date) VALUES (1, 1, 2, '2020-02-12 12:12:07-07');
INSERT INTO requests(customer_id, plot_id, employee_id, processed_date) VALUES (3, 2, 1, '2022-06-02 07:55:32-07');
INSERT INTO requests(customer_id, plot_id, employee_id, processed_date) VALUES (2, 3, 3, '2023-05-04 03:13:44-07');
INSERT INTO requests(customer_id, plot_id, employee_id, processed_date) VALUES (5, 4, 4, '2021-12-17 02:44:23-07');
INSERT INTO requests(customer_id, plot_id, employee_id, processed_date) VALUES (4, 5, 5, '2025-04-26 09:21:56-07');

-- 7. Индексы для ускорения работы приложения
-- Поиск свободных мест будет очень частым
CREATE INDEX IF NOT EXISTS idx_plots_status_sector ON plots(sector_id, status);
-- Поиск заявок по статусу (для панели администратора)
CREATE INDEX IF NOT EXISTS idx_requests_status ON requests(status);
-- Поиск всех заявок конкретного клиента (для личного кабинета)
CREATE INDEX IF NOT EXISTS idx_requests_customer ON requests(customer_id);