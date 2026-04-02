import pandas as pd
import numpy as np
import os
import sys
import matplotlib.pyplot as plt

# 解决 Windows 终端编码问题
try:
    if sys.stdout.encoding.lower() != 'utf-8':
        sys.stdout.reconfigure(encoding='utf-8')
except:
    pass

import xgboost as xgb
from xgboost import XGBClassifier
from sklearn.model_selection import train_test_split

import onnxmltools
from onnxmltools.convert import convert_xgboost
from onnxmltools.convert.common.data_types import FloatTensorType

def train_and_export():
    print(f"[Info] XGBoost version: {xgb.__version__}")
    
    # 1. 路径与配置
    data_path = 'train_data.csv'
    model_onnx_path = 'cargo_risk_model.onnx'
    plot_path = 'feature_importance.png'

    if not os.path.exists(data_path):
        print(f"[Error] data not found: {data_path}")
        return

    # 2. 加载数据
    print("[Info] Loading data...")
    df = pd.read_csv(data_path)
    features = ['register_days', 'product_count', 'report_count', 'avg_reply_time', 'credit_score']
    target = 'is_violation'
    
    X = df[features].values.astype(np.float32)
    y = df[target].values

    # 3. 数据划分
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 4. 训练模型
    print("[Info] Training model...")
    model = XGBClassifier(
        n_estimators=100, 
        max_depth=4, 
        learning_rate=0.1, 
        objective='binary:logistic',
        use_label_encoder=False,
        eval_metric='logloss'
    )
    model.fit(X_train, y_train)
    
    # 5. 校验
    accuracy = model.score(X_test, y_test)
    print(f"[Success] Model accuracy: {accuracy:.4f}")

    # 6. 特征重要性图中文化移除及绘制
    print("[Info] Plotting feature importance...")
    plt.figure(figsize=(10, 6))
    importances = model.feature_importances_
    indices = np.argsort(importances)
    plt.title('Feature Importances')
    plt.barh(range(len(indices)), importances[indices], color='skyblue', align='center')
    plt.yticks(range(len(indices)), [features[i] for i in indices])
    plt.xlabel('Importance')
    plt.tight_layout()
    plt.savefig(plot_path)
    print(f"[Success] Plot saved to: {plot_path}")

    # 7. 导出 ONNX (直接使用 Booster 转换以避免 XGBClassifier 算子解析错误)
    print("[Info] Exporting to ONNX format...")
    
    # 获取底层的 Booster 对象
    booster = model.get_booster()
    
    # 指定输入张量名称和层级
    initial_type = [('float_input', FloatTensorType([None, 5]))]
    
    try:
        # 使用 onnxmltools.convert.convert_xgboost 直接对 booster 转换
        # 这是转换 XGBoost 最稳定的方式，特别是针对 1.x 和 2.x 版本
        onnx_model = convert_xgboost(
            booster, 
            initial_types=initial_type, 
            target_opset=12
        )
        
        # 将模型写入本地
        with open(model_onnx_path, "wb") as f:
            f.write(onnx_model.SerializeToString())
            
        print(f"[Success] Model exported to: {model_onnx_path}")
        
    except Exception as e:
        print(f"[Error] ONNX export failed: {e}")
        # 最后的尝试: 注册后进行整体模型转换 (如果 Booster 转换也失败)
        try:
            print("[Info] Final attempt with registration...")
            from skl2onnx import update_registered_converter
            from onnxmltools.convert.xgboost.operator_converters.XGBoost import convert_xgboost as xgb_converter
            from skl2onnx.common.shape_calculator import calculate_linear_classifier_output_shapes
            
            update_registered_converter(
                XGBClassifier, 'XGBoostXGBClassifier',
                calculate_linear_classifier_output_shapes, xgb_converter
            )
            onnx_model_final = onnxmltools.convert_xgboost(model, initial_types=initial_type, target_opset=12)
            with open(model_onnx_path, "wb") as f:
                f.write(onnx_model_final.SerializeToString())
            print(f"[Success] Model exported with registration: {model_onnx_path}")
        except Exception as e2:
            print(f"[Critical Error] All conversion attempts failed: {e2}")

if __name__ == "__main__":
    train_and_export()
