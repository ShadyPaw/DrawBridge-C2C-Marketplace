# 🛒 闲置集市 (CargoCO) - 二手闲置物品交易平台 V1

> 一个基于 Spring Boot + Vue 3 的 C2C 二手物品交易系统，仿闲鱼设计，支持商品发布、订单交易、信用评价、后台管理等完整交易闭环。

![Java](https://img.shields.io/badge/Java-1.8+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green)
![Vue](https://img.shields.io/badge/Vue-3.x-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [功能模块](#功能模块)
- [数据库设计](#数据库设计)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [API 接口说明](#api-接口说明)
- [截图预览](#截图预览)

---

## 项目简介

随着共享经济和绿色消费理念的普及，闲置物品交易已成为大众共识。本项目旨在构建一个面向个人用户的 C2C 闲置物品交易平台，解决闲置物品"流通渠道窄、交易效率低"的痛点。

**核心特点：**
- 🔐 JWT 无状态鉴权 + BCrypt 密码加密
- 📦 完整的商品发布 → 审核 → 上架 → 交易闭环
- 💳 订单全生命周期管理（下单 → 付款 → 发货 → 收货）
- ⭐ 信用积分评价体系（好评加分、差评扣分、等级自动升降）
- 🛡️ 举报机制 + 后台审核管理
- 💬 商品留言沟通（多级回复）
- 📱 响应式设计，适配多终端

---

## 技术栈

### 后端
| 技术 | 说明 |
|------|------|
| Spring Boot 2.7.18 | 应用框架 |
| MyBatis | ORM 持久层 |
| MySQL 8.0 | 关系型数据库 |
| PageHelper | MyBatis 分页插件 |
| JWT (jjwt 0.9.1) | 用户认证 Token |
| Spring Security Crypto | BCrypt 密码加密 |
| Lombok | 简化 Java Bean |

### 前端
| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| Element Plus | UI 组件库 |
| Vite | 构建工具 |
| Vue Router 4 | 路由管理 |
| Pinia | 状态管理 |
| Axios | HTTP 请求库 |

---

## 功能模块

### 🏠 前台功能

#### 1. 用户模块
- 用户注册/登录（JWT Token 鉴权）
- 个人信息管理（昵称、手机、邮箱、性别）
- 密码修改
- 收货地址管理（多地址、默认地址）

#### 2. 商品模块
- 商品发布（标题、描述、价格、成色、图片上传、分类选择、交易方式）
- 商品浏览（分页、分类筛选、关键词搜索）
- 多维排序（最新、价格升降、热度）
- 商品详情（图片轮播、卖家信息、留言区、收藏）
- 我的发布管理（上架/下架/删除）

#### 3. 订单模块
- 一键下单购买
- 买家视角：我买到的（付款、确认收货、取消）
- 卖家视角：我卖出的（确认发货）
- 订单详情查看
- 完整状态流转：待付款 → 待发货 → 待收货 → 已完成/已取消

#### 4. 互动模块
- 商品收藏/取消收藏
- 商品留言（多级回复）
- 交易评价（好评/中评/差评 + 文字评价）

#### 5. 信用体系
- 初始信用积分 100 分
- 好评 +5 分 / 差评 -10 分
- 自动等级调整：普通用户 → 信用良好 → 优质用户

#### 6. 举报机制
- 支持举报商品/用户/留言
- 多种举报原因（虚假信息、违禁物品、欺诈行为等）

### 🔧 后台管理

| 功能 | 说明 |
|------|------|
| 控制台 | 用户数、商品数、订单数、待审核/待处理统计 |
| 用户管理 | 用户列表搜索、启用/禁用账号 |
| 商品审核 | 审核待发布商品（通过/拒绝 + 备注） |
| 订单管理 | 全部订单查看、状态筛选 |
| 举报管理 | 举报列表、处理/驳回 + 处理说明 |
| 公告管理 | 系统公告的发布/编辑/删除 |
| 分类管理 | 商品分类树形管理（支持多级分类） |

---

## 数据库设计

本系统共设计 **14 张数据表**，涵盖用户、商品、交易、信用、管理等核心业务。

### 数据库 ER 关系概览

```
用户(user) ──┬── 收货地址(user_address)
             ├── 商品(product) ── 商品图片(product_image)
             │        └── 留言(message)
             │        └── 收藏(favorite)
             ├── 订单(order_info) ── 评价(review)
             ├── 举报(report)
             ├── 信用记录(credit_record)
             └── 通知(notice)
```

### 详细表结构

#### 1. `user` - 用户信息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 用户ID |
| username | VARCHAR(50) | 登录账号（唯一） |
| password | VARCHAR(255) | BCrypt 加密密码 |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| phone | VARCHAR(20) | 手机号（唯一） |
| email | VARCHAR(100) | 邮箱 |
| gender | TINYINT | 性别：0-未知, 1-男, 2-女 |
| real_name | VARCHAR(50) | 真实姓名（实名认证） |
| id_card | VARCHAR(20) | 身份证号 |
| credit_score | INT | 信用积分（默认100） |
| user_level | TINYINT | 等级：1-普通, 2-信用良好, 3-优质 |
| status | TINYINT | 状态：0-禁用, 1-正常 |
| role | TINYINT | 角色：0-普通用户, 1-管理员 |
| last_login_time | DATETIME | 最后登录时间 |
| create_time | DATETIME | 注册时间 |
| deleted | TINYINT | 逻辑删除标记 |

#### 2. `user_address` - 收货地址表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 地址ID |
| user_id | BIGINT (FK) | 所属用户 |
| receiver_name | VARCHAR(50) | 收货人姓名 |
| receiver_phone | VARCHAR(20) | 收货人电话 |
| province / city / district | VARCHAR(50) | 省/市/区 |
| detail_address | VARCHAR(255) | 详细地址 |
| is_default | TINYINT | 是否默认地址 |

#### 3. `category` - 商品分类表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 分类ID |
| parent_id | BIGINT | 父分类ID（0=顶级） |
| name | VARCHAR(50) | 分类名称 |
| icon | VARCHAR(500) | 分类图标 |
| sort_order | INT | 排序序号 |
| status | TINYINT | 状态：0-禁用, 1-启用 |

#### 4. `product` - 商品信息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 商品ID |
| user_id | BIGINT (FK) | 发布者ID |
| category_id | BIGINT (FK) | 分类ID |
| title | VARCHAR(100) | 商品标题 |
| description | TEXT | 商品描述 |
| original_price | DECIMAL(10,2) | 原价 |
| price | DECIMAL(10,2) | 出售价 |
| quality | TINYINT | 成色：1-全新 ~ 5-其他 |
| location | VARCHAR(200) | 交易地点 |
| trade_mode | TINYINT | 交易方式：1-邮寄, 2-自提, 3-均可 |
| view_count | INT | 浏览量 |
| favorite_count | INT | 收藏数 |
| product_status | TINYINT | 状态：1-在售, 2-下架, 3-已售 |
| audit_status | TINYINT | 审核：0-待审, 1-通过, 2-拒绝 |
| audit_remark | VARCHAR(255) | 审核备注 |

#### 5. `product_image` - 商品图片表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 图片ID |
| product_id | BIGINT (FK) | 商品ID |
| image_url | VARCHAR(500) | 图片URL |
| is_cover | TINYINT | 是否封面图 |
| sort_order | INT | 排序 |

#### 6. `message` - 留言信息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 留言ID |
| product_id | BIGINT (FK) | 所属商品 |
| user_id | BIGINT (FK) | 留言者 |
| parent_id | BIGINT | 父留言ID（0=顶级） |
| reply_user_id | BIGINT | 被回复用户 |
| content | TEXT | 留言内容 |

#### 7. `order_info` - 订单信息表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 订单ID |
| order_no | VARCHAR(64) | 订单编号（唯一） |
| product_id | BIGINT (FK) | 商品ID |
| seller_id | BIGINT (FK) | 卖家ID |
| buyer_id | BIGINT (FK) | 买家ID |
| address_id | BIGINT (FK) | 收货地址ID |
| price | DECIMAL(10,2) | 成交价 |
| order_status | TINYINT | 0-待付款, 1-待发货, 2-待收货, 3-完成, 4-取消 |
| pay_time / ship_time / receive_time | DATETIME | 各阶段时间 |
| cancel_reason | VARCHAR(255) | 取消原因 |
| remark | VARCHAR(500) | 订单备注 |

#### 8. `review` - 评价表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 评价ID |
| order_id | BIGINT (FK) | 订单ID |
| from_user_id / to_user_id | BIGINT (FK) | 评价人/被评价人 |
| type | TINYINT | 1-买家评卖家, 2-卖家评买家 |
| rating | TINYINT | 1-差评, 2-中评, 3-好评 |
| content | TEXT | 评价内容 |
| is_anonymous | TINYINT | 是否匿名 |

#### 9. `favorite` - 收藏表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 收藏ID |
| user_id | BIGINT (FK) | 用户ID |
| product_id | BIGINT (FK) | 商品ID |
| 唯一约束 | | user_id + product_id |

#### 10. `report` - 举报表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 举报ID |
| reporter_id | BIGINT (FK) | 举报人 |
| target_type | TINYINT | 1-商品, 2-用户, 3-留言 |
| target_id | BIGINT | 目标ID |
| reason_type | TINYINT | 1-虚假 ~ 5-其他 |
| handle_status | TINYINT | 0-待处理 ~ 3-已驳回 |
| handle_user_id | BIGINT | 处理人（管理员） |

#### 11. `notice` - 通知公告表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT (PK) | 通知ID |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| type | TINYINT | 1-系统公告, 2-交易通知, 3-审核, 4-举报 |
| target_user_id | BIGINT | 目标用户（NULL=全体） |
| is_read | TINYINT | 是否已读 |

#### 12. `operation_log` - 操作日志表
记录管理员的关键操作，用于安全审计。

#### 13. `credit_record` - 信用记录表
记录用户信用积分的每次变动明细，支撑信用体系的可追溯性。

#### 14. `banner` - 轮播图表
首页轮播图/广告位管理。

---

## 项目结构

```
CargoCO/
├── sql/
│   └── create_tables.sql              # 数据库建表 + 初始数据脚本
│
├── backend/                            # Spring Boot 后端
│   ├── pom.xml                         # Maven 依赖配置
│   └── src/main/
│       ├── java/com/cargoco/
│       │   ├── CargoCOApplication.java # 启动类
│       │   ├── common/                 # 统一响应封装 (Result, PageResult)
│       │   ├── config/                 # 配置类 (CORS, WebMvc, 全局异常)
│       │   ├── controller/             # REST API 控制器 (13个)
│       │   ├── entity/                 # 数据实体类 (14个)
│       │   ├── interceptor/            # JWT 登录拦截器
│       │   ├── mapper/                 # MyBatis Mapper 接口 (13个)
│       │   ├── service/                # 业务逻辑层 (接口+实现)
│       │   └── utils/                  # 工具类 (JwtUtil)
│       └── resources/
│           ├── application.yml         # 应用配置
│           └── mapper/                 # MyBatis XML 映射文件 (13个)
│
├── frontend/                           # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/index.js                # API 接口封装
│       ├── assets/styles/global.css    # 全局样式
│       ├── components/                 # 公共组件
│       │   ├── AppHeader.vue           # 顶部导航栏
│       │   ├── AppFooter.vue           # 页脚
│       │   └── ProductCard.vue         # 商品卡片
│       ├── router/index.js             # 路由配置
│       ├── stores/user.js              # Pinia 用户状态
│       ├── utils/request.js            # Axios 请求封装
│       └── views/                      # 页面组件
│           ├── Home.vue                # 首页
│           ├── Login.vue / Register.vue
│           ├── Search.vue              # 搜索结果
│           ├── ProductDetail.vue       # 商品详情
│           ├── Publish.vue             # 发布闲置
│           ├── OrderDetail.vue         # 订单详情
│           ├── UserProfile.vue         # 用户主页
│           ├── my/                     # 个人中心 (8个子页面)
│           └── admin/                  # 管理后台 (8个子页面)
│
└── README.md
```

---

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+
- npm 8+

### 1. 克隆项目
```bash
git clone https://github.com/ShadyPaw/CargoTradeV1.git
cd CargoTradeV1
```

### 2. 初始化数据库
```sql
-- 登录 MySQL 执行建表脚本
mysql -u root -p -P 3308 < sql/create_tables.sql
```
> 脚本会自动创建 `cargo_co` 数据库、14 张表和初始数据（管理员账号 + 默认分类 + 欢迎公告）

### 3. 配置后端
编辑 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3308/cargo_co?...  # 修改端口
    username: root                                   # 修改用户名
    password: 123456                                 # 修改密码

file:
  upload:
    path: E:/CargoCO/uploads/                        # 修改为你的本地路径
```

### 4. 启动后端
```bash
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

### 5. 启动前端
```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

### 6. 访问系统
- 前台首页: http://localhost:5173
- 管理后台: http://localhost:5173/admin

**默认管理员账号：**
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

---

## API 接口说明

### 认证方式
- 登录接口返回 JWT Token
- 后续请求在 Header 中携带：`Authorization: Bearer <token>`

### 主要接口分组

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 用户 | `/api/user` | 注册、登录、个人信息 |
| 商品 | `/api/product` | CRUD、搜索、筛选 |
| 分类 | `/api/category` | 列表、树形结构 |
| 订单 | `/api/order` | 下单、付款、发货、收货 |
| 留言 | `/api/message` | 发送、查询 |
| 评价 | `/api/review` | 提交评价 |
| 收藏 | `/api/favorite` | 收藏/取消、列表 |
| 举报 | `/api/report` | 提交举报 |
| 通知 | `/api/notice` | 通知列表 |
| 地址 | `/api/address` | CRUD |
| 文件 | `/api/file` | 图片上传 |
| 管理 | `/api/admin` | 后台全部管理接口 |

---

## 设计亮点

1. **信用体系闭环**：评价 → 信用分变动 → 等级自动调整 → 信用记录可追溯
2. **商品审核机制**：发布后需管理员审核通过方可展示，保障平台内容质量
3. **完整订单流转**：支持待付款/待发货/待收货/已完成/已取消全状态管理
4. **全表逻辑删除**：数据不物理删除，通过 `deleted` 字段标记，保障数据安全
5. **统一异常处理**：全局异常捕获，前端获得友好的错误提示
6. **响应式设计**：移动端/PC端自适应布局

---

## 📝 迭代日志 (Changelog)

### V1.4 (2026年4月1日)
- **实时“已读/未读”双向通讯**：重构底层 WebSocket 协议，接入 `type` 鉴权帧，实现会话气泡毫秒级无刷新状态跳变与精确未读计数秒消。
- **自动运营大屏**：全局 Navbar 下植入首页公告走马灯悬浮轮播系统，自动拦截展现后端设定的最新 5 条系统级广播。
- **防御连环死锁**：根治不填手机号注册空串强推 MySQL 后引发的崩溃级账户幽灵态“不存在”连环判错死锁，加入安全的数据脱水机制。
- **UI 及会话修复**：剥离重构了前端 Websocekt 全局独立广播域解耦，修缮全局 css `.price` 货币符号重复显示故障，并联通后端展示聊天上下文的真实商品售价。

---

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

> 📧 如有问题或建议，欢迎提 Issue 或 Pull Request！

---

## Current Iteration

- Current version: `V1.5.0`
- Updated on: `2026-04-02`

### V1.5 (2026-04-02)

- Refined the marketplace home and header UX with a cleaner top navigation, dynamic category naming, and an expanded integrated search bar.
- Added a fixed right-side floating action panel with publish, message, feedback, support, and back-to-top actions.
- Removed duplicated top-level entry points that overlapped with the floating panel, giving more space back to search.
- Redesigned the message center with a cleaner conversation layout, improved empty states, refreshed message bubbles, and a more polished input area.
- Fixed the floating message badge logic so the red dot only appears when unread messages actually exist.
- Updated header behavior so the top search area no longer stays fixed while scrolling, and system notices can now be dismissed manually.
