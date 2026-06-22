import os
import json
import torch
import torch.nn as nn
import torchvision.models as models

def main():
    print("Setting up Plant Disease Detection model export...")
    
    # 38 PlantVillage Classes
    classes = [
        "Apple___Apple_scab", "Apple___Black_rot", "Apple___Cedar_apple_rust", "Apple___healthy",
        "Blueberry___healthy", "Cherry_(including_sour)___Powdery_mildew", "Cherry_(including_sour)___healthy",
        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot", "Corn_(maize)___Common_rust_",
        "Corn_(maize)___Northern_Leaf_Blight", "Corn_(maize)___healthy", "Grape___Black_rot",
        "Grape___Esca_(Black_Measles)", "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", "Grape___healthy",
        "Orange___Haunglongbing_(Citrus_greening)", "Peach___Bacterial_spot", "Peach___healthy",
        "Pepper,_bell___Bacterial_spot", "Pepper,_bell___healthy", "Potato___Early_blight",
        "Potato___Late_blight", "Potato___healthy", "Raspberry___healthy", "Soybean___healthy",
        "Squash___Powdery_mildew", "Strawberry___Leaf_scorch", "Strawberry___healthy",
        "Tomato___Bacterial_spot", "Tomato___Early_blight", "Tomato___Late_blight",
        "Tomato___Leaf_Mold", "Tomato___Septoria_leaf_spot", "Tomato___Spider_mites Two-spotted_spider_mite",
        "Tomato___Target_Spot", "Tomato___Tomato_Yellow_Leaf_Curl_Virus", "Tomato___Tomato_mosaic_virus",
        "Tomato___healthy"
    ]
    
    # Save disease labels
    os.makedirs("output", exist_ok=True)
    labels_mapping = {int(idx): name for idx, name in enumerate(classes)}
    with open("output/disease_labels.json", "w") as f:
        json.dump(labels_mapping, f, indent=4)
    print("Saved disease_labels.json")
    
    # Define a MobileNetV2 model architecture with 38 outputs
    print("Initializing MobileNetV2 architecture...")
    model = models.mobilenet_v2(weights=None)
    model.classifier[1] = nn.Linear(model.last_channel, len(classes))
    
    # Put model in evaluation mode
    model.eval()
    
    # Dummy input matching the preprocessing shape [Batch, Channels, Height, Width]
    # Standard input size is 224x224
    dummy_input = torch.randn(1, 3, 224, 224, requires_grad=True)
    
    # Export the model
    print("Exporting model to ONNX format...")
    torch.onnx.export(
        model,
        dummy_input,
        "output/plant_disease.onnx",
        export_params=True,
        opset_version=11,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes={'input': {0: 'batch_size'}, 'output': {0: 'batch_size'}}
    )
    print("Exported plant_disease.onnx successfully")

if __name__ == "__main__":
    main()
