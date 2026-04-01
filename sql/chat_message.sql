USE cargo_co;
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
    `sender_id`       BIGINT        NOT NULL                 COMMENT '发送者用户ID',
    `receiver_id`     BIGINT        NOT NULL                 COMMENT '接收者用户ID',
    `product_id`      BIGINT        DEFAULT NULL             COMMENT '关联商品ID',
    `content`         TEXT          NOT NULL                 COMMENT '消息内容',
    `is_read`         TINYINT       DEFAULT 0                COMMENT '已读状态: 0-未读, 1-已读',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_conversation` (`sender_id`, `receiver_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊消息表';
