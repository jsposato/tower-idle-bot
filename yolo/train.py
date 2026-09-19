import sys

sys.path.append('/home/shino/.local/share/pipx/venvs/ultralytics/lib/python3.13/site-packages')

from ultralytics import YOLO

# Load the model.
model = YOLO('yolo11n.pt')

# Training.
results = model.train(
    data='ttb.yaml',
    imgsz=640,
    epochs=2500,
    batch=22,
    patience=1000,
    iou=0.95,
    name='yolo_ttb')
