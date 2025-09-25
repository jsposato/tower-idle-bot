import sys
sys.path.append('/home/shino/.local/share/pipx/venvs/ultralytics/lib/python3.13/site-packages')

import os
import shutil
from ultralytics import YOLO

model = YOLO("./runs/detect/yolo_ttb/weights/best.pt")

# Pre label other pictures using the trained model
# Here is a guide on how to import it into label studio: https://labelstud.io/blog/tutorial-importing-local-yolo-pre-annotated-images-to-label-studio/
folder_path = 'data'
file_paths = ['./' + os.path.join(folder_path, filename) for filename in os.listdir(folder_path) if os.path.isfile(os.path.join(folder_path, filename))]

results = model(file_paths)

# Process results list
for result in results:
    boxes = result.boxes  # Boxes object for bounding box outputs
    masks = result.masks  # Masks object for segmentation masks outputs
    keypoints = result.keypoints  # Keypoints object for pose outputs
    probs = result.probs  # Probs object for classification outputs
    obb = result.obb  # Oriented boxes object for OBB outputs

    cls = boxes.cls
    xywhn = boxes.xywhn
    labelBoxes = []
    for i in range(cls.size(0)):
        labelBoxes.append([int(cls[i].item())] + xywhn[i].tolist())

    filename = os.path.splitext(os.path.basename(result.path))[0]
    shutil.copy(result.path, "./data_labeled/images/")
    with open('./data_labeled/labels/' + filename + '.txt', 'w') as f:
        for item in labelBoxes:
            f.write(f"{' '.join(map(str, item))}\n")

    # result.save(filename="./test_result.png")  # save to disk