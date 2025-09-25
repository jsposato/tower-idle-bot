import sys
sys.path.append('/home/shino/.local/share/pipx/venvs/ultralytics/lib/python3.13/site-packages')
sys.path.append('/home/shino/.local/share/pipx/venvs/onnxslim/lib/python3.13/site-packages')
from ultralytics import YOLO

model = YOLO("./runs/detect/yolo_ttb/weights/best.pt")

model.export(format="onnx")