-- 1. Bảng lưu trữ đơn hàng
CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT,
    order_code   VARCHAR(50)    NOT NULL,
    user_id      BIGINT         NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status       ENUM('PENDING', 'PARTIALLY_PAID', 'PAID', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    version      INT            NOT NULL DEFAULT 0,
    expires_at   TIMESTAMP      NULL,
    paid_at      TIMESTAMP      NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_orders_order_code UNIQUE (order_code),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)

) ENGINE=InnoDB;