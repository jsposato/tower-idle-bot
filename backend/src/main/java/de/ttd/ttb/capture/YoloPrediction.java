package de.ttd.ttb.capture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YoloPrediction {
  private static final String MODEL_PATH = "./ttb.onnx";
  private static final int TARGET_IMG_HEIGHT = 640;
  private static final int TARGET_IMG_WIDTH = 640;
  private static final int ORIGINAL_IMG_HEIGHT = 1280;
  private static final int ORIGINAL_IMG_WIDTH = 720;
  private static final float SCALE_FACTOR = 1f / 255f;
  private static final int NUM_CLASSES = 33;
  private static final float CONFIDENCE_THRESHOLD = 0.9f;

  private static final Net model;

  static {
    OpenCV.loadShared();
    model = Dnn.readNetFromONNX(MODEL_PATH);
  }

  public List<BoxPrediction> predict(
      BufferedImage image, Map<Prediction, Float> confidenceThresholds) {
    final var matImage = bufferedImageToMat(image);
    final var blob = preprocess(matImage);
    return getBoxPredictions(blob, confidenceThresholds);
    // for (final var boxPrediction : result) {
    //   Imgproc.rectangle(
    //       matImage,
    //       boxPrediction.getBox().tl(),
    //       boxPrediction.getBox().br(),
    //       new Scalar(0, 255, 0),
    //       2);
    // }
    // Imgcodecs.imwrite("output.jpg", matImage);
  }

  public static Mat bufferedImageToMat(BufferedImage bufferedImage) {
    // Check if the BufferedImage is in the correct format
    if (bufferedImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
      // Convert the BufferedImage to TYPE_3BYTE_BGR if necessary
      BufferedImage convertedImage =
          new BufferedImage(
              bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      convertedImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
      bufferedImage = convertedImage;
    }

    // Get the byte array from the BufferedImage
    byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();

    // Create a new Mat object with the same dimensions and type as the BufferedImage
    Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);

    // Copy the pixel data from the byte array to the Mat object
    mat.put(0, 0, pixels);

    return mat;
  }

  /**
   * Get the predicted boxes for a preprocessed blob. This method applies non-max suppression to
   * filter the results.
   */
  private static List<BoxPrediction> getBoxPredictions(
      Mat blob, Map<Prediction, Float> confidenceThresholds) {
    // YoloV9 output is batch_size x (num_classes + 4) x 8400
    // where the + 4 in the second dimension refers to the
    // center_x,center_y,width,height of the detection box
    // Since we're only looking at one sample, we reshape it to 8400 rows to get rid
    // of the batch dimension
    // YoloV9 output: batch size x n_classes + 4 x 8400. Batch size is 1 so we
    // remove that dimension by reshaping it to 49 rows where 49 = n_classes + 4
    // That way, the model outputs are now arranged in 8400 columns where each
    // column value represents a box anchor.
    // Each row of a column represents the probability for a given class
    // The first through fourth rows of each column is x,y,w,h
    // In other words, for each of the 8400 anchors (columns) the first 4 rows
    // represent the box location, and the rest of the values represent the
    // probabilities that box is class N
    // For each column, we want to get the maximum probability.
    // If the maximum probability is greater than our determined threshold, we will
    // consider that an answer and add it to our list
    // We will create a rectangle for the answers, and store the class names as
    // well.
    List<BoxPrediction> preds = new ArrayList<>();
    model.setInput(blob);
    Mat output = model.forward().reshape(0, NUM_CLASSES + 4);
    Mat confidences;
    float confidence;
    Mat column;
    Core.MinMaxLocResult mm;
    Point center;
    Rect box;
    int width;
    int height;
    int centerX;
    int centerY;
    for (int i = 0; i < output.cols(); i++) {
      column = output.col(i);
      confidences = column.rowRange(4, NUM_CLASSES + 4);
      mm = Core.minMaxLoc(confidences);
      confidence = (float) mm.maxVal;
      final var prediction = Prediction.values()[(int) mm.maxLoc.y];
      final var confidenceThreshold =
          confidenceThresholds.getOrDefault(prediction, CONFIDENCE_THRESHOLD);

      if (confidence > confidenceThreshold) {
        centerX = (int) column.get(0, 0)[0];
        centerY = (int) column.get(1, 0)[0];
        width = (int) column.get(2, 0)[0];
        height = (int) column.get(3, 0)[0];

        centerX =
            (int) ((double) centerX * ((double) ORIGINAL_IMG_WIDTH / (double) TARGET_IMG_WIDTH));
        centerY =
            (int) ((double) centerY * ((double) ORIGINAL_IMG_HEIGHT / (double) TARGET_IMG_HEIGHT));
        width = (int) ((double) width * ((double) ORIGINAL_IMG_WIDTH / (double) TARGET_IMG_WIDTH));
        height =
            (int) ((double) height * ((double) ORIGINAL_IMG_HEIGHT / (double) TARGET_IMG_HEIGHT));

        int x = centerX - width / 2;
        int y = centerY - height / 2;

        center = new Point(x, y);
        box = new Rect(center, new Size(width, height));

        preds.add(new BoxPrediction(box, new Pixel(x, y), prediction));
      }
    }
    return preds;
  }

  private static Mat preprocess(Mat image) {
    return Dnn.blobFromImage(
        image,
        SCALE_FACTOR,
        new Size(TARGET_IMG_WIDTH, TARGET_IMG_HEIGHT),
        new Scalar(0, 0, 0),
        true,
        false);
  }
}
