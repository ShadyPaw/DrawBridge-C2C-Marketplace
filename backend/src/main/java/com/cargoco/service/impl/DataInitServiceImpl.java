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
 * 社区演示数据初始化服务。
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

    private static final DemoUserSeed[] DEMO_USERS = {
            new DemoUserSeed("user_demo_1", "考研退坑的学妹", "13800001001", 128, 2),
            new DemoUserSeed("user_demo_2", "数码宅老王", "13800001002", 145, 2),
            new DemoUserSeed("user_demo_3", "大四毕业清仓", "13800001003", 118, 2),
            new DemoUserSeed("user_demo_4", "爱跑步的小陈", "13800001004", 132, 2),
            new DemoUserSeed("user_demo_5", "文艺女青年小苏", "13800001005", 156, 2),
            new DemoUserSeed("user_demo_6", "搬家断舍离", "13800001006", 112, 2),
            new DemoUserSeed("user_demo_7", "程序员阿杰", "13800001007", 164, 2),
            new DemoUserSeed("user_demo_8", "宝妈闲置多", "13800001008", 120, 2),
            new DemoUserSeed("user_demo_9", "摄影爱好者老李", "13800001009", 138, 2),
            new DemoUserSeed("user_demo_10", "健身达人小周", "13800001010", 172, 2),
            new DemoUserSeed("risk_demo_1", "被举报较多的老周", "13800002001", 38, 1),
            new DemoUserSeed("risk_demo_2", "新号急售的小许", "13800002002", 49, 1),
            new DemoUserSeed("risk_demo_3", "交易记录稀少的小吴", "13800002003", 63, 1),
            new DemoUserSeed("risk_demo_4", "中风险样本阿杰", "13800002004", 76, 1),
            new DemoUserSeed("risk_demo_5", "观察中的小林", "13800002005", 88, 1)
    };

    private static final String[][] PRODUCTS = {
            {"女生自用九成新 iPad Air 5", "2022 年购入，一直贴膜戴壳使用，无明显磕碰，电池健康度 96%，配件齐全。", "4799", "3200", "2", "3", "1"},
            {"考研英语红宝书真题整套", "备考用书，几乎没有笔记，内页干净，适合继续刷题复习。", "189", "45", "2", "7", "3"},
            {"Cherry MX3.0S 机械键盘", "红轴手感很舒服，用了两个月后换静音键盘，包装配件都在。", "599", "320", "2", "6", "1"},
            {"耐克 Air Max 270 男鞋 42 码", "穿了不到 10 次，鞋底磨损很少，不合脚所以转出。", "899", "350", "3", "4", "3"},
            {"大疆 Mini 3 无人机套装", "带畅飞配件包和两块电池，飞行次数不多，状态很好。", "4288", "2800", "2", "5", "1"},
            {"宜家 KALLAX 书架 白色", "搬家带不走，4x2 格，八成新，仅限同城自提。", "599", "150", "3", "7", "2"},
            {"AirPods Pro 2 USB-C 版", "AppleCare+ 到明年，降噪效果很好，换新耳机后出掉。", "1899", "1100", "2", "4", "1"},
            {"CPA 教材加网课笔记", "会计网校轻松过关系列，六科全，重点标注清晰。", "360", "80", "3", "7", "1"},
            {"小米净水器滤芯三只装全新", "买多了用不完，全新未拆封，型号 600G 增强版通用。", "297", "180", "1", "7", "1"},
            {"Switch 健身环大冒险 国行", "已通关，健身环和游戏卡都完好。", "499", "200", "3", "6", "1"},
            {"雅马哈 FG800 民谣吉他", "新手练习琴，音色不错，送琴包和调音器。", "1580", "650", "3", "5", "2"},
            {"Herman Miller Aeron 人体工学椅", "公司搬迁处理，B Size，功能正常，坐感很舒服。", "9800", "3500", "3", "7", "2"},
            {"索尼 WH-1000XM5 头戴耳机", "银色款，日版购入，降噪很强，配件和发票都在。", "2999", "1600", "2", "4", "1"},
            {"数据结构与算法分析（第三版）", "计算机经典教材，少量铅笔标注可擦除。", "79", "25", "3", "7", "1"},
            {"戴森 V12 吸尘器", "用了一年，吸力依然很稳，配件齐全。", "4490", "2200", "3", "7", "1"},
            {"Lululemon Align 瑜伽裤 黑色 S 码", "穿了两次发现尺码不合适，洗过一次，无起球。", "850", "450", "2", "4", "1"},
            {"iPad 手写笔记模板合集", "自制考研和读书模板 50 多套，购买后发下载链接。", "0", "9.9", "1", "8", "1"},
            {"美的电饭煲 4L 智能预约", "搬家换小尺寸，这台功能正常，九成新。", "399", "120", "2", "7", "1"},
            {"PS5 光驱版加双手柄加 3 款游戏", "通关后整套出，机器和配件都保护得不错。", "4299", "2800", "3", "6", "1"},
            {"北面 1996 羽绒服 L 码", "经典黑色，穿了一个冬天，已干洗，保暖很好。", "1898", "800", "3", "4", "1"}
    };

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
            "https://picsum.photos/seed/prod20/800/600"
    };

    @Override
    @Transactional
    public void initCommunityData() {
        User admin = userMapper.findByUsername("admin");
        if (admin != null) {
            userMapper.updatePassword(admin.getId(), passwordEncoder.encode("admin123"));
        }

        List<Long> userIds = new ArrayList<>();
        List<Long> riskUserIds = new ArrayList<>();
        String defaultPassword = passwordEncoder.encode("123456");

        for (int i = 0; i < DEMO_USERS.length; i++) {
            DemoUserSeed seed = DEMO_USERS[i];
            User existing = userMapper.findByUsername(seed.username);
            Long currentUserId;

            if (existing != null) {
                existing.setNickname(seed.nickname);
                existing.setPhone(seed.phone);
                existing.setAvatar(buildAvatar(seed.username));
                existing.setEmail(seed.username + "@demo.com");
                existing.setGender(i % 3);
                userMapper.update(existing);
                userMapper.updateCreditScore(existing.getId(), seed.creditScore, seed.userLevel);
                currentUserId = existing.getId();
            } else {
                User user = new User();
                user.setUsername(seed.username);
                user.setPassword(defaultPassword);
                user.setNickname(seed.nickname);
                user.setPhone(seed.phone);
                user.setAvatar(buildAvatar(seed.username));
                user.setGender(i % 3);
                user.setEmail(seed.username + "@demo.com");
                user.setStatus(1);
                user.setRole(0);
                user.setCreditScore(seed.creditScore);
                user.setUserLevel(seed.userLevel);
                userMapper.insert(user);
                currentUserId = user.getId();
            }

            userIds.add(currentUserId);
            if (seed.creditScore < 100) {
                riskUserIds.add(currentUserId);
            }
        }

        String[] locations = {"北京海淀", "上海浦东", "广州天河", "深圳南山", "杭州西湖", "成都武侯", "南京鼓楼", "武汉洪山", "西安雁塔", "长沙岳麓"};

        for (int i = 0; i < PRODUCTS.length; i++) {
            String[] productSeed = PRODUCTS[i];
            Product product = new Product();
            product.setUserId(pickSellerId(i, userIds, riskUserIds));
            product.setCategoryId(Long.parseLong(productSeed[5]));
            product.setTitle(productSeed[0]);
            product.setDescription(productSeed[1]);
            product.setOriginalPrice(new BigDecimal(productSeed[2]));
            product.setPrice(new BigDecimal(productSeed[3]));
            product.setQuality(Integer.parseInt(productSeed[4]));
            product.setLocation(locations[random.nextInt(locations.length)]);
            product.setTradeMode(Integer.parseInt(productSeed[6]));
            product.setProductStatus(1);
            product.setAuditStatus(1);
            product.setViewCount(random.nextInt(500) + 10);
            product.setFavoriteCount(random.nextInt(50));
            productMapper.insert(product);

            ProductImage coverImage = new ProductImage();
            coverImage.setProductId(product.getId());
            coverImage.setImageUrl(PRODUCT_IMAGES[i]);
            coverImage.setIsCover(1);
            coverImage.setSortOrder(0);
            productImageMapper.insert(coverImage);

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

    private String buildAvatar(String username) {
        return "https://api.dicebear.com/7.x/adventurer/svg?seed=" + username;
    }

    private Long pickSellerId(int index, List<Long> userIds, List<Long> riskUserIds) {
        if (!riskUserIds.isEmpty() && index < riskUserIds.size()) {
            return riskUserIds.get(index);
        }
        return userIds.get(random.nextInt(userIds.size()));
    }

    private static final class DemoUserSeed {
        private final String username;
        private final String nickname;
        private final String phone;
        private final Integer creditScore;
        private final Integer userLevel;

        private DemoUserSeed(String username, String nickname, String phone, Integer creditScore, Integer userLevel) {
            this.username = username;
            this.nickname = nickname;
            this.phone = phone;
            this.creditScore = creditScore;
            this.userLevel = userLevel;
        }
    }
}