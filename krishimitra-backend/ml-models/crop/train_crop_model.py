import os
import json
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

def main():
    print("Starting Crop Recommendation model training...")
    
    # 1. Generate synthetic crop recommendation data similar to the standard Kaggle dataset
    # Features: N, P, K, temperature, humidity, ph, rainfall
    # Labels: 22 crops
    crops = [
        "rice", "maize", "chickpea", "kidneybeans", "pigeonpeas",
        "mothbeans", "mungbean", "blackgram", "lentil", "pomegranate",
        "banana", "mango", "grapes", "watermelon", "muskmelon",
        "apple", "orange", "papaya", "coconut", "cotton",
        "jute", "coffee"
    ]
    
    # Generate 100 samples per crop (2200 total)
    np.random.seed(42)
    data = []
    for crop in crops:
        for _ in range(100):
            # Baseline parameters for realistic crop features
            if crop == "rice":
                row = [np.random.uniform(70, 95), np.random.uniform(35, 55), np.random.uniform(35, 45),
                       np.random.uniform(20, 27), np.random.uniform(80, 85), np.random.uniform(5.5, 6.9), np.random.uniform(180, 290)]
            elif crop == "maize":
                row = [np.random.uniform(60, 100), np.random.uniform(35, 60), np.random.uniform(15, 25),
                       np.random.uniform(18, 27), np.random.uniform(55, 70), np.random.uniform(5.7, 7.0), np.random.uniform(60, 110)]
            elif crop == "cotton":
                row = [np.random.uniform(100, 140), np.random.uniform(30, 50), np.random.uniform(15, 25),
                       np.random.uniform(22, 29), np.random.uniform(75, 85), np.random.uniform(5.8, 8.0), np.random.uniform(60, 100)]
            elif crop == "jute":
                row = [np.random.uniform(60, 100), np.random.uniform(35, 50), np.random.uniform(35, 45),
                       np.random.uniform(23, 27), np.random.uniform(70, 90), np.random.uniform(6.0, 7.8), np.random.uniform(150, 205)]
            elif crop == "coffee":
                row = [np.random.uniform(80, 120), np.random.uniform(15, 30), np.random.uniform(25, 35),
                       np.random.uniform(23, 28), np.random.uniform(50, 60), np.random.uniform(6.0, 7.5), np.random.uniform(115, 150)]
            else:
                # Generic fallback for other crops
                row = [np.random.uniform(20, 100), np.random.uniform(20, 80), np.random.uniform(10, 50),
                       np.random.uniform(15, 35), np.random.uniform(40, 90), np.random.uniform(5.0, 8.0), np.random.uniform(30, 250)]
            data.append(row + [crop])
            
    df = pd.DataFrame(data, columns=["N", "P", "K", "temperature", "humidity", "ph", "rainfall", "label"])
    
    X = df.drop(columns=["label"]).values.astype(np.float32)
    y = df["label"].values
    
    # Encode labels
    le = LabelEncoder()
    y_encoded = le.fit_transform(y)
    
    # Save label encoder mapping
    label_mapping = {int(idx): str(name) for idx, name in enumerate(le.classes_)}
    os.makedirs("output", exist_ok=True)
    with open("output/crop_labels.json", "w") as f:
        json.dump(label_mapping, f, indent=4)
    print("Saved crop_labels.json")
    
    # Train/Test Split
    X_train, X_test, y_train, y_test = train_test_split(X, y_encoded, test_size=0.2, random_state=42)
    
    # 2. Train Random Forest model
    clf = RandomForestClassifier(n_estimators=100, max_depth=12, random_state=42)
    clf.fit(X_train, y_train)
    
    y_pred = clf.predict(X_test)
    print("Classification Report:")
    print(classification_report(y_test, y_pred, target_names=le.classes_))
    
    # 3. Export to ONNX
    initial_type = [('float_input', FloatTensorType([None, 7]))]
    onnx_model = convert_sklearn(clf, initial_types=initial_type, target_opset=12)
    
    with open("output/crop_recommendation.onnx", "wb") as f:
        f.write(onnx_model.SerializeToString())
    print("Saved crop_recommendation.onnx successfully")

if __name__ == "__main__":
    main()
