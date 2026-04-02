import csv
import random
import os

def generate_mock_data(num_samples=5000):
    # 固定随机种子确保结果可复现
    random.seed(42)
    
    data = []
    violation_count = 0
    
    for user_id in range(1, num_samples + 1):
        # 1. register_days (注册天数): 大部分是老用户，呈正态分布在半年左右，做上下限裁剪
        register_days = int(random.gauss(180, 120))
        register_days = max(1, min(register_days, 1000))
        
        # 2. product_count (发布商品数): 大部分人商品较少，呈指数分布，部分是重度卖家/黑产发帖机
        if random.random() < 0.05: # 5% 的用户发布较多商品
            product_count = random.randint(10, 50)
            # 为了制造一些特定违规特征，让这其中一部分用户是新注册的
            if random.random() < 0.3:
                register_days = random.randint(1, 6) # 注册 < 7天
        else:
            product_count = int(random.expovariate(1/4)) # 平均发布4个
        
        # 3. report_count (被举报次数): 绝大多数是0，少数是被频繁举报的用户
        if random.random() < 0.05: # 5% 的问题用户
            report_count = random.randint(2, 10)
        else:
            report_count = 0 if random.random() < 0.8 else random.randint(1, 2)
            
        # 4. avg_reply_time (平均回复耗时，单位小时): 正常用户可能回复快也可能慢
        avg_reply_time = round(random.gauss(6, 8), 1)
        avg_reply_time = max(0.1, min(avg_reply_time, 72.0))
        
        # 5. credit_score (当前信用分): 以100分为基准正态分布，会有坏账用户的低分
        if random.random() < 0.05: # 5% 信用极低
            credit_score = int(random.gauss(55, 10))
        else:
            credit_score = int(random.gauss(95, 10))
        
        credit_score = max(0, min(credit_score, 120))
        
        # --- 风险标签注入（打标） ---
        # 如果满足以下任一条件，将 is_violation 设为 1（高风险违规）：
        # 1) report_count > 3 且 avg_reply_time > 12
        # 2) register_days < 7 且 product_count > 15
        # 3) credit_score < 60
        is_violation = 0
        if (report_count > 3 and avg_reply_time > 12) or \
           (register_days < 7 and product_count > 15) or \
           (credit_score < 60):
            is_violation = 1
            violation_count += 1
            
        data.append([
            user_id, 
            register_days, 
            product_count, 
            report_count, 
            avg_reply_time, 
            credit_score, 
            is_violation
        ])
        
    headers = ['user_id', 'register_days', 'product_count', 'report_count', 'avg_reply_time', 'credit_score', 'is_violation']
    
    # 输出到 train_data.csv 文件
    output_file = 'train_data.csv'
    with open(output_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        writer.writerows(data)
        
    # 打印统计结果
    print(f"✅ 成功生成 {num_samples} 条数据并保存至 {output_file}")
    print(f"📊 违规样本 (is_violation=1): {violation_count} (占比: {violation_count/num_samples:.2%})")
    print(f"🌟 正常样本 (is_violation=0): {num_samples - violation_count} (占比: {(num_samples - violation_count)/num_samples:.2%})")

if __name__ == "__main__":
    generate_mock_data()
