-- ===================================================================
-- 闲置物品交易系统 数据库建表脚本
-- 技术栈: SpringBoot + Vue + MySQL
-- 创建时间: 2026-04-01
-- 说明: 基于开题报告中的功能需求设计，涵盖用户管理、商品管理、
--       留言沟通、订单管理、评价体系、举报机制、后台管理等模块
-- ===================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `cargo_co` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `cargo_co`;

-- ===================================================================
-- 1. 用户信息表 (user)
-- 说明: 存储系统注册用户的基本信息及信用体系相关字段
-- ===================================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '用户ID（主键）',
    `username`        VARCHAR(50)   NOT NULL                 COMMENT '用户名（登录账号）',
    `password`        VARCHAR(255)  NOT NULL                 COMMENT '密码（加密存储）',
    `nickname`        VARCHAR(50)   DEFAULT NULL             COMMENT '昵称',
    `avatar`          VARCHAR(500)  DEFAULT NULL             COMMENT '头像URL',
    `phone`           VARCHAR(20)   DEFAULT NULL             COMMENT '手机号',
    `email`           VARCHAR(100)  DEFAULT NULL             COMMENT '邮箱',
    `gender`          TINYINT       DEFAULT 0                COMMENT '性别: 0-未知, 1-男, 2-女',
    `real_name`       VARCHAR(50)   DEFAULT NULL             COMMENT '真实姓名（实名认证）',
    `id_card`         VARCHAR(20)   DEFAULT NULL             COMMENT '身份证号（实名认证）',
    `credit_score`    INT           DEFAULT 100              COMMENT '信用积分（初始100分）',
    `user_level`      TINYINT       DEFAULT 1                COMMENT '用户等级: 1-普通用户, 2-信用良好, 3-优质用户',
    `status`          TINYINT       DEFAULT 1                COMMENT '账号状态: 0-禁用, 1-正常, 2-待审核',
    `role`            TINYINT       DEFAULT 0                COMMENT '角色: 0-普通用户, 1-管理员',
    `last_login_time` DATETIME      DEFAULT NULL             COMMENT '最后登录时间',
    `last_login_ip`   VARCHAR(50)   DEFAULT NULL             COMMENT '最后登录IP',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';


-- ===================================================================
-- 2. 用户收货地址表 (user_address)
-- 说明: 存储用户的收货地址信息，支持多地址管理
-- ===================================================================
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '地址ID（主键）',
    `user_id`         BIGINT        NOT NULL                 COMMENT '用户ID（外键）',
    `receiver_name`   VARCHAR(50)   NOT NULL                 COMMENT '收货人姓名',
    `receiver_phone`  VARCHAR(20)   NOT NULL                 COMMENT '收货人电话',
    `province`        VARCHAR(50)   NOT NULL                 COMMENT '省份',
    `city`            VARCHAR(50)   NOT NULL                 COMMENT '城市',
    `district`        VARCHAR(50)   DEFAULT NULL             COMMENT '区/县',
    `detail_address`  VARCHAR(255)  NOT NULL                 COMMENT '详细地址',
    `is_default`      TINYINT       DEFAULT 0                COMMENT '是否默认地址: 0-否, 1-是',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';


-- ===================================================================
-- 3. 商品分类表 (category)
-- 说明: 存储商品分类信息，支持多级分类（树形结构）
-- ===================================================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '分类ID（主键）',
    `parent_id`       BIGINT        DEFAULT 0                COMMENT '父分类ID（0表示顶级分类）',
    `name`            VARCHAR(50)   NOT NULL                 COMMENT '分类名称',
    `icon`            VARCHAR(500)  DEFAULT NULL             COMMENT '分类图标URL',
    `sort_order`      INT           DEFAULT 0                COMMENT '排序序号（升序）',
    `status`          TINYINT       DEFAULT 1                COMMENT '状态: 0-禁用, 1-启用',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';


-- ===================================================================
-- 4. 商品信息表 (product)
-- 说明: 存储闲置商品的核心信息，包含审核状态字段（后台审核机制）
-- ===================================================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '商品ID（主键）',
    `user_id`         BIGINT        NOT NULL                 COMMENT '发布者用户ID（外键）',
    `category_id`     BIGINT        DEFAULT NULL             COMMENT '商品分类ID（外键）',
    `title`           VARCHAR(100)  NOT NULL                 COMMENT '商品标题',
    `description`     TEXT          DEFAULT NULL             COMMENT '商品描述',
    `original_price`  DECIMAL(10,2) DEFAULT NULL             COMMENT '商品原价',
    `price`           DECIMAL(10,2) NOT NULL                 COMMENT '出售价格',
    `quality`         TINYINT       DEFAULT 5                COMMENT '成色/新旧程度: 1-全新, 2-几乎全新, 3-轻微使用痕迹, 4-明显使用痕迹, 5-其他',
    `location`        VARCHAR(200)  DEFAULT NULL             COMMENT '交易地点/发货地',
    `trade_mode`      TINYINT       DEFAULT 1                COMMENT '交易方式: 1-邮寄, 2-自提/面交, 3-均可',
    `view_count`      INT           DEFAULT 0                COMMENT '浏览次数',
    `favorite_count`  INT           DEFAULT 0                COMMENT '收藏次数',
    `product_status`  TINYINT       DEFAULT 1                COMMENT '商品状态: 1-在售, 2-已下架, 3-已售出',
    `audit_status`    TINYINT       DEFAULT 0                COMMENT '审核状态: 0-待审核, 1-审核通过, 2-审核拒绝',
    `audit_remark`    VARCHAR(255)  DEFAULT NULL             COMMENT '审核备注（拒绝原因等）',
    `audit_time`      DATETIME      DEFAULT NULL             COMMENT '审核时间',
    `audit_user_id`   BIGINT        DEFAULT NULL             COMMENT '审核人ID',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_product_status` (`product_status`),
    KEY `idx_audit_status` (`audit_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品信息表';


-- ===================================================================
-- 5. 商品图片表 (product_image)
-- 说明: 存储商品的多张图片信息（一对多关系）
-- ===================================================================
DROP TABLE IF EXISTS `product_image`;
CREATE TABLE `product_image` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '图片ID（主键）',
    `product_id`      BIGINT        NOT NULL                 COMMENT '商品ID（外键）',
    `image_url`       VARCHAR(500)  NOT NULL                 COMMENT '图片URL',
    `is_cover`        TINYINT       DEFAULT 0                COMMENT '是否封面图: 0-否, 1-是',
    `sort_order`      INT           DEFAULT 0                COMMENT '排序序号',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';


-- ===================================================================
-- 6. 留言信息表 (message)
-- 说明: 存储商品下的留言信息，支持多级回复（买卖双方沟通）
-- ===================================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '留言ID（主键）',
    `product_id`      BIGINT        NOT NULL                 COMMENT '所属商品ID（外键）',
    `user_id`         BIGINT        NOT NULL                 COMMENT '留言用户ID（外键）',
    `parent_id`       BIGINT        DEFAULT 0                COMMENT '父留言ID（0表示顶级留言，非0表示回复）',
    `reply_user_id`   BIGINT        DEFAULT NULL             COMMENT '被回复的用户ID',
    `content`         TEXT          NOT NULL                 COMMENT '留言内容',
    `status`          TINYINT       DEFAULT 1                COMMENT '状态: 0-隐藏, 1-正常',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '留言时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='留言信息表';


-- ===================================================================
-- 7. 订单信息表 (order_info)
-- 说明: 存储交易订单信息，管理完整的交易流程
--       注意: 表名使用 order_info 避免与MySQL保留字 order 冲突
-- ===================================================================
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '订单ID（主键）',
    `order_no`        VARCHAR(64)   NOT NULL                 COMMENT '订单编号（唯一，如时间戳+随机数）',
    `product_id`      BIGINT        NOT NULL                 COMMENT '商品ID（外键）',
    `seller_id`       BIGINT        NOT NULL                 COMMENT '卖家用户ID（外键）',
    `buyer_id`        BIGINT        NOT NULL                 COMMENT '买家用户ID（外键）',
    `address_id`      BIGINT        DEFAULT NULL             COMMENT '收货地址ID（外键）',
    `price`           DECIMAL(10,2) NOT NULL                 COMMENT '成交价格',
    `trade_mode`      TINYINT       DEFAULT 1                COMMENT '交易方式: 1-邮寄, 2-自提/面交, 3-均可',
    `order_status`    TINYINT       DEFAULT 0                COMMENT '订单状态: 0-待付款, 1-待发货, 2-待收货, 3-已完成, 4-已取消, 5-退款中, 6-已退款',
    `pay_time`        DATETIME      DEFAULT NULL             COMMENT '支付时间',
    `ship_time`       DATETIME      DEFAULT NULL             COMMENT '发货时间',
    `receive_time`    DATETIME      DEFAULT NULL             COMMENT '收货/确认时间',
    `cancel_time`     DATETIME      DEFAULT NULL             COMMENT '取消时间',
    `cancel_reason`   VARCHAR(255)  DEFAULT NULL             COMMENT '取消原因',
    `remark`          VARCHAR(500)  DEFAULT NULL             COMMENT '订单备注',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单信息表';


-- ===================================================================
-- 8. 用户评价表 (review)
-- 说明: 存储交易完成后买卖双方的互评信息（信用体系核心）
-- ===================================================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '评价ID（主键）',
    `order_id`        BIGINT        NOT NULL                 COMMENT '订单ID（外键）',
    `product_id`      BIGINT        NOT NULL                 COMMENT '商品ID（外键）',
    `from_user_id`    BIGINT        NOT NULL                 COMMENT '评价人用户ID',
    `to_user_id`      BIGINT        NOT NULL                 COMMENT '被评价人用户ID',
    `type`            TINYINT       DEFAULT 1                COMMENT '评价类型: 1-买家评卖家, 2-卖家评买家',
    `rating`          TINYINT       NOT NULL                 COMMENT '评分: 1-差评, 2-中评, 3-好评',
    `content`         TEXT          DEFAULT NULL             COMMENT '评价内容',
    `is_anonymous`    TINYINT       DEFAULT 0                COMMENT '是否匿名: 0-否, 1-是',
    `status`          TINYINT       DEFAULT 1                COMMENT '状态: 0-隐藏, 1-正常',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_from_user_id` (`from_user_id`),
    KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评价表';


-- ===================================================================
-- 9. 收藏表 (favorite)
-- 说明: 存储用户收藏/关注的商品信息
-- ===================================================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '收藏ID（主键）',
    `user_id`         BIGINT        NOT NULL                 COMMENT '用户ID（外键）',
    `product_id`      BIGINT        NOT NULL                 COMMENT '商品ID（外键）',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';


-- ===================================================================
-- 10. 举报表 (report)
-- 说明: 存储用户对商品或其他用户的违规举报信息
-- ===================================================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '举报ID（主键）',
    `reporter_id`     BIGINT        NOT NULL                 COMMENT '举报人用户ID（外键）',
    `target_type`     TINYINT       NOT NULL                 COMMENT '举报目标类型: 1-商品, 2-用户, 3-留言',
    `target_id`       BIGINT        NOT NULL                 COMMENT '举报目标ID（商品ID/用户ID/留言ID）',
    `reason_type`     TINYINT       DEFAULT 1                COMMENT '举报原因类型: 1-虚假信息, 2-违禁物品, 3-欺诈行为, 4-不当言论, 5-其他',
    `reason_detail`   TEXT          DEFAULT NULL             COMMENT '举报详细说明',
    `evidence_urls`   VARCHAR(2000) DEFAULT NULL             COMMENT '举证图片URL（多个用逗号分隔）',
    `handle_status`   TINYINT       DEFAULT 0                COMMENT '处理状态: 0-待处理, 1-处理中, 2-已处理, 3-已驳回',
    `handle_result`   VARCHAR(500)  DEFAULT NULL             COMMENT '处理结果说明',
    `handle_user_id`  BIGINT        DEFAULT NULL             COMMENT '处理人（管理员）ID',
    `handle_time`     DATETIME      DEFAULT NULL             COMMENT '处理时间',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';


-- ===================================================================
-- 11. 系统通知/公告表 (notice)
-- 说明: 存储系统公告和用户个人通知
-- ===================================================================
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '通知ID（主键）',
    `title`           VARCHAR(200)  NOT NULL                 COMMENT '通知标题',
    `content`         TEXT          NOT NULL                 COMMENT '通知内容',
    `type`            TINYINT       DEFAULT 1                COMMENT '通知类型: 1-系统公告, 2-交易通知, 3-审核通知, 4-举报处理通知',
    `target_user_id`  BIGINT        DEFAULT NULL             COMMENT '目标用户ID（NULL表示全体用户公告）',
    `sender_id`       BIGINT        DEFAULT NULL             COMMENT '发送人ID（管理员ID，系统通知为NULL）',
    `is_read`         TINYINT       DEFAULT 0                COMMENT '是否已读: 0-未读, 1-已读',
    `status`          TINYINT       DEFAULT 1                COMMENT '状态: 0-下架, 1-正常',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_target_user_id` (`target_user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知公告表';


-- ===================================================================
-- 12. 操作日志表 (operation_log)
-- 说明: 记录管理员和系统的关键操作日志，用于安全审计
-- ===================================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '日志ID（主键）',
    `user_id`         BIGINT        DEFAULT NULL             COMMENT '操作人用户ID',
    `username`        VARCHAR(50)   DEFAULT NULL             COMMENT '操作人用户名',
    `operation`       VARCHAR(100)  NOT NULL                 COMMENT '操作描述',
    `method`          VARCHAR(200)  DEFAULT NULL             COMMENT '请求方法',
    `request_url`     VARCHAR(500)  DEFAULT NULL             COMMENT '请求URL',
    `request_params`  TEXT          DEFAULT NULL             COMMENT '请求参数',
    `ip`              VARCHAR(50)   DEFAULT NULL             COMMENT '操作IP地址',
    `status`          TINYINT       DEFAULT 1                COMMENT '操作结果: 0-失败, 1-成功',
    `error_msg`       TEXT          DEFAULT NULL             COMMENT '错误信息',
    `cost_time`       BIGINT        DEFAULT NULL             COMMENT '耗时（毫秒）',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';


-- ===================================================================
-- 13. 信用记录表 (credit_record)
-- 说明: 记录用户信用积分的变动明细（信用体系核心支撑表）
-- ===================================================================
DROP TABLE IF EXISTS `credit_record`;
CREATE TABLE `credit_record` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '记录ID（主键）',
    `user_id`         BIGINT        NOT NULL                 COMMENT '用户ID（外键）',
    `change_type`     TINYINT       NOT NULL                 COMMENT '变动类型: 1-交易完成(加分), 2-获得好评(加分), 3-获得差评(扣分), 4-被举报成立(扣分), 5-违规处罚(扣分), 6-管理员调整',
    `change_value`    INT           NOT NULL                 COMMENT '变动分值（正数为加分，负数为扣分）',
    `before_score`    INT           NOT NULL                 COMMENT '变动前积分',
    `after_score`     INT           NOT NULL                 COMMENT '变动后积分',
    `related_id`      BIGINT        DEFAULT NULL             COMMENT '关联业务ID（订单ID/举报ID等）',
    `remark`          VARCHAR(255)  DEFAULT NULL             COMMENT '变动说明',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信用记录表';


-- ===================================================================
-- 14. 轮播图/广告位表 (banner)
-- 说明: 存储首页轮播图或推广位信息（后台管理配置）
-- ===================================================================
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '轮播图ID（主键）',
    `title`           VARCHAR(100)  DEFAULT NULL             COMMENT '标题',
    `image_url`       VARCHAR(500)  NOT NULL                 COMMENT '图片URL',
    `link_url`        VARCHAR(500)  DEFAULT NULL             COMMENT '跳转链接',
    `sort_order`      INT           DEFAULT 0                COMMENT '排序序号（升序）',
    `status`          TINYINT       DEFAULT 1                COMMENT '状态: 0-禁用, 1-启用',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT       DEFAULT 0                COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图广告位表';


-- ===================================================================
-- 初始化数据
-- ===================================================================

-- 插入管理员账号（密码为 admin123 的 BCrypt 加密结果）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `credit_score`, `user_level`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm0g3hJMhfd6v7vNqZvK', '系统管理员', 1, 1, 100, 3);

-- 插入默认商品分类
INSERT INTO `category` (`parent_id`, `name`, `sort_order`, `status`) VALUES
(0, '电子产品',   1, 1),
(0, '书籍教材',   2, 1),
(0, '生活用品',   3, 1),
(0, '服装鞋包',   4, 1),
(0, '运动户外',   5, 1),
(0, '美妆护肤',   6, 1),
(0, '家居家电',   7, 1),
(0, '其他',       8, 1);

-- 插入二级分类（以"电子产品"和"书籍教材"为例）
INSERT INTO `category` (`parent_id`, `name`, `sort_order`, `status`) VALUES
(1, '手机',       1, 1),
(1, '平板电脑',   2, 1),
(1, '笔记本电脑', 3, 1),
(1, '耳机音箱',   4, 1),
(1, '相机摄影',   5, 1),
(1, '其他数码',   6, 1),
(2, '教材课本',   1, 1),
(2, '考试辅导',   2, 1),
(2, '文学小说',   3, 1),
(2, '专业书籍',   4, 1),
(2, '其他书籍',   5, 1);

-- 插入系统公告示例
INSERT INTO `notice` (`title`, `content`, `type`, `target_user_id`, `status`) VALUES
('欢迎使用闲置物品交易平台', '欢迎来到闲置物品交易平台！在这里您可以发布闲置物品、浏览和购买他人的闲置商品。请遵守平台规则，诚信交易。', 1, NULL, 1);


-- ===================================================================
-- 15. 私聊消息表 (chat_message)
-- 说明: 存储用户之间的实时私聊消息，支持商品上下文关联
-- ===================================================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '消息ID（主键）',
    `sender_id`       BIGINT        NOT NULL                 COMMENT '发送者用户ID',
    `receiver_id`     BIGINT        NOT NULL                 COMMENT '接收者用户ID',
    `product_id`      BIGINT        DEFAULT NULL             COMMENT '关联商品ID（可为空，表示非商品场景聊天）',
    `content`         TEXT          NOT NULL                 COMMENT '消息内容',
    `is_read`         TINYINT       DEFAULT 0                COMMENT '已读状态: 0-未读, 1-已读',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_conversation` (`sender_id`, `receiver_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊消息表';
