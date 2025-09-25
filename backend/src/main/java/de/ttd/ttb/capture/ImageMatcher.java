package de.ttd.ttb.capture;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class ImageMatcher {

  public static double difference(
      BufferedImage img1,
      BufferedImage img2,
      int box_x_min,
      int box_x_max,
      int box_y_min,
      int box_y_max) {
    int[] hist1 = calculateHistogram(img1, box_x_min, box_x_max, box_y_min, box_y_max);
    int[] hist2 = calculateHistogram(img2, box_x_min, box_x_max, box_y_min, box_y_max);
    double difference = 0.0;

    for (int i = 0; i < hist1.length; i++) {
      difference += Math.abs(hist1[i] - hist2[i]);
    }

    return difference;
  }

  private static int[] calculateHistogram(
      BufferedImage image, int box_x_min, int box_x_max, int box_y_min, int box_y_max) {
    int[] histogram = new int[256]; // For grayscale

    Raster raster = image.getRaster();
    for (int y = box_y_min; y < image.getHeight() && y <= box_y_max; y++) {
      for (int x = box_x_min; x < image.getWidth() && x <= box_x_max; x++) {
        int pixelValue = raster.getSample(x, y, 0); // Assuming grayscale
        histogram[pixelValue]++;
      }
    }

    return histogram;
  }
}
