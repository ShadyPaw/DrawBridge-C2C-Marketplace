package com.cargoco.service.impl;

import com.cargoco.entity.Product;
import com.cargoco.entity.ProductImage;
import com.cargoco.entity.User;
import com.cargoco.mapper.ProductImageMapper;
import com.cargoco.mapper.ProductMapper;
import com.cargoco.mapper.UserMapper;
import com.cargoco.service.DataInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 社区生态数据初始化服务
 * 创建模拟用户和商品，用于系统演示和测试
 */
@Service
public class DataInitServiceImpl implements DataInitService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductImageMapper productImageMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Random random = new Random();

    // 10 个具有中国 C2C 特色的用户
    private static final String[] USER_NAMES = {
            "考研退坑的学姐", "数码宅老王", "大四毕业清仓", "爱跑步的小陈",
            "文艺女青年小苏", "搬家断舍离", "程序员阿杰", "宝妈闲置多",
            "摄影爱好者老李", "健身达人小周"
    };

    private static final String[] USER_PHONES = {
            "13800001001", "13800001002", "13800001003", "13800001004",
            "13800001005", "13800001006", "13800001007", "13800001008",
            "13800001009", "13800001010"
    };

    // 20 个逼真的二手商品
    private static final String[][] PRODUCTS = {
            // {标题, 描述, 原价, 售价, 成色(1-5), 分类ID, 交易方式(1-3)}
            {"女生自用九成新 iPad Air5", "22年购入，一直贴膜戴壳使用，无划痕无磕碰，电池健康度96%，配件齐全", "4799", "3200", "2", "3", "1"},
            {"考研英语红宝书+真题全套", "26考研用书，只翻了几页就上岸了，内页干净无笔记，送荧光笔", "189", "45", "2", "7", "3"},
            {"Cherry 樱桃 MX3.0S 机械键盘", "红轴手感超棒，买了两个月换了静电容，包装配件都在", "599", "320", "2", "6", "1"},
            {"耐克 Air Max 270 男鞋 42码", "去年双11买的，穿了不到10次，鞋底几乎没磨损，不合脚出", "899", "350", "3", "4", "3"},
            {"大疆 Mini 3 无人机套装", "带畅飞配件包、3块电池、ND滤镜套装，飞行记录不到20次", "4288", "2800", "2", "5", "1"},
            {"宜家 KALLAX 书架 白色", "搬家带不走，4x2格，八成新，自提可小刀，仅限同城", "599", "150", "3", "7", "2"},
            {"AirPods Pro 2 USB-C版", "AppleCare+到25年底，降噪效果很好，换了新耳机出掉", "1899", "1100", "2", "4", "1"},
            {"全套注会CPA教材+网课笔记", "中华会计网校轻松过关系列，6科全，笔记详细标注重点", "360", "80", "3", "7", "1"},
            {"小米净水器滤芯 三只装全新", "买多了用不完，全新未拆封，型号400G增强版通用", "297", "180", "1", "7", "1"},
            {"Switch 健身环大冒险 国行", "通关了出掉，健身环+游戏卡带，功能完好", "499", "200", "3", "6", "1"},

            {"雅马哈 FG800 民谣吉他", "新手练习琴，音色不错，送琴包调音器变调夹全套配件", "1580", "650", "3", "5", "2"},
            {"Herman Miller Aeron 人体工学椅", "公司搬迁处理，B size，功能全正常，坐感极佳", "9800", "3500", "3", "7", "2"},
            {"索尼 WH-1000XM5 头戴降噪耳机", "银色款，日版购入，降噪天花板，配件齐全带发票", "2999", "1600", "2", "4", "1"},
            {"数据结构与算法分析（第三版）", "计算机专业经典教材，有少量铅笔标注可擦除", "79", "25", "3", "7", "1"},
            {"戴森 V12 吸尘器", "买了一年，吸力依然强劲，配件齐全，女生自用很爱惜", "4490", "2200", "3", "7", "1"},

            {"Lululemon Align 瑜伽裤 黑色 S码", "穿了两次觉得尺码不合适，洗过一次，无起球", "850", "450", "2", "4", "1"},
            {"iPad 手写笔记本 GoodNotes 模板合集", "自制考研/读书笔记模板50+套，购买后发链接", "0", "9.9", "1", "8", "1"},
            {"美的电饭煲 4L 智能预约", "搬家换了个小的，这个功能全好，煮饭香，九成新", "399", "120", "2", "7", "1"},
            {"PS5 光驱版 + 双手柄 + 3游戏", "艾尔登法环/战神/蜘蛛侠2，通关了全出，箱说全", "4299", "2800", "3", "6", "1"},
            {"North Face 北面 1996 羽绒服 L码", "经典黑色款，去年冬天穿了一季，已干洗，保暖没得说", "1898", "800", "3", "4", "1"},
    };

    // 使用 picsum.photos 作为稳定的测试图片源
    private static final String[] PRODUCT_IMAGES = {
            "https://picsum.photos/seed/prod1/800/600",
            "https://picsum.photos/seed/prod2/800/600",
            "https://picsum.photos/seed/prod3/800/600",
            "https://picsum.photos/seed/prod4/800/600",
            "https://picsum.photos/seed/prod5/800/600",
            "https://picsum.photos/seed/prod6/800/600",
            "https://picsum.photos/seed/prod7/800/600",
            "https://picsum.photos/seed/prod8/800/600",
            "https://picsum.photos/seed/prod9/800/600",
            "https://picsum.photos/seed/prod10/800/600",
            "https://picsum.photos/seed/prod11/800/600",
            "https://picsum.photos/seed/prod12/800/600",
            "https://picsum.photos/seed/prod13/800/600",
            "https://picsum.photos/seed/prod14/800/600",
            "https://picsum.photos/seed/prod15/800/600",
            "https://picsum.photos/seed/prod16/800/600",
            "https://picsum.photos/seed/prod17/800/600",
            "https://picsum.photos/seed/prod18/800/600",
            "https://picsum.photos/seed/prod19/800/600",
            "https://picsum.photos/seed/prod20/800/600",
    };

    @Override
    @Transactional
    public void initCommunityData() {
        // ========== 第零步：修复管理员密码 ==========
        User admin = userMapper.findByUsername("admin");
        if (admin != null) {
            userMapper.updatePassword(admin.getId(), passwordEncoder.encode("admin123"));
        }

        // ========== 第一步：插入模拟用户 ==========
        List<Long> userIds = new ArrayList<>();
        String defaultPassword = passwordEncoder.encode("123456");

        for (int i = 0; i < USER_NAMES.length; i++) {
            // 检查用户名是否已存在，避免重复初始化
            String username = "user_demo_" + (i + 1);
            User existing = userMapper.findByUsername(username);
            if (existing != null) {
                userIds.add(existing.getId());
                continue;
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(defaultPassword);
            user.setNickname(USER_NAMES[i]);
            user.setPhone(USER_PHONES[i]);
            user.setAvatar("https://api.dicebear.com/7.x/adventurer/svg?seed=" + username);
            user.setGender(random.nextInt(3)); // 0-未知, 1-男, 2-女
            user.setEmail(username + "@demo.com");
            user.setStatus(1);
            user.setRole(0);
            user.setCreditScore(100);
            user.setUserLevel(1);
            userMapper.insert(user);
            userIds.add(user.getId());
        }

        // ========== 第二步：插入模拟商品 ==========
        String[] locations = {"北京海淀", "上海浦东", "广州天河", "深圳南山", "杭州西湖",
                "成都武侯", "南京鼓楼", "武汉洪山", "西安雁塔", "长沙岳麓"};

        for (int i = 0; i < PRODUCTS.length; i++) {
            String[] p = PRODUCTS[i];
            Product product = new Product();
            // 随机分配给一个用户
            product.setUserId(userIds.get(random.nextInt(userIds.size())));
            product.setCategoryId(Long.parseLong(p[5]));
            product.setTitle(p[0]);
            product.setDescription(p[1]);
            product.setOriginalPrice(new BigDecimal(p[2]));
            product.setPrice(new BigDecimal(p[3]));
            product.setQuality(Integer.parseInt(p[4]));
            product.setLocation(locations[random.nextInt(locations.length)]);
            product.setTradeMode(Integer.parseInt(p[6]));
            product.setProductStatus(1); // 在售
            product.setAuditStatus(1);   // 审核通过（模拟数据直接通过）
            product.setViewCount(random.nextInt(500) + 10);
            product.setFavoriteCount(random.nextInt(50));

            productMapper.insert(product);

            // 插入封面图片
            ProductImage coverImage = new ProductImage();
            coverImage.setProductId(product.getId());
            coverImage.setImageUrl(PRODUCT_IMAGES[i]);
            coverImage.setIsCover(1);
            coverImage.setSortOrder(0);
            productImageMapper.insert(coverImage);

            // 随机插入 1-2 张额外图片
            int extraCount = random.nextInt(2) + 1;
            for (int j = 0; j < extraCount; j++) {
                ProductImage extraImage = new ProductImage();
                extraImage.setProductId(product.getId());
                extraImage.setImageUrl("https://picsum.photos/seed/extra" + i + "_" + j + "/800/600");
                extraImage.setIsCover(0);
                extraImage.setSortOrder(j + 1);
                productImageMapper.insert(extraImage);
            }
        }
    }
}
