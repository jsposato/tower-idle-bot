package de.ttd.ttb.capture;

import java.awt.image.BufferedImage;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageReader {
  private static final Tesseract tesseract;

  static {
    tesseract = new Tesseract();
    tesseract.setDatapath("tessdata");
    tesseract.setLanguage("eng");
  }

  public String read(
      BufferedImage image, int box_x_min, int box_x_max, int box_y_min, int box_y_max)
      throws Exception {
    final var croppedImage =
        image.getSubimage(box_x_min, box_y_min, box_x_max - box_x_min, box_y_max - box_y_min);
    return tesseract.doOCR(croppedImage);
  }
}
