package de.ttd.ttb.capture;

import de.ttd.ttb.Device;
import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaptureService {
  // TODO: Move into hasPrediction function
  private static final Map<Prediction, Float> CONFIDENCE_THRESHOLDS =
      new HashMap<>() {
        {
          put(Prediction.GA_Ad_Black_Screen, 0.5f);
          put(Prediction.GA_Claim_Gems, 0.5f);
          put(Prediction.GA_Close_Ad, 0.5f);
          put(Prediction.B_New_Perk, 0.95f);
          put(Prediction.B_Perk_Window, 0.5f);
          put(Prediction.B_All_Coin_Bonuses, 0.5f);
          put(Prediction.B_Failed_To_Load_Ad_Ok, 0.5f);
          put(Prediction.B_Menu_Closed, 0.97f);
          put(Prediction.B_Slot, 0.95f);
          put(Prediction.B_Slot_Button, 0.95f);
          put(Prediction.B_Slot_Label, 0.95f);
          put(Prediction.B_Slot_Price, 0.95f);
          put(Prediction.B_Slot_Value, 0.95f);
          put(Prediction.B_Slot_Maxed, 0.95f);
          put(Prediction.O_Battle, 0.5f);
          put(Prediction.B_Tab_Attack, 0.65f);
          put(Prediction.B_Tab_Defense, 0.65f);
          put(Prediction.B_Tab_Utility, 0.65f);
          put(Prediction.B_Floating_Gem, 0.65f);
        }
      };

  private final Device device;
  private final ImageReader reader;
  private final YoloPrediction yoloPrediction;

  private BufferedImage currentImage;
  @Getter private List<BoxPrediction> currentPredictions;

  @PostConstruct
  private void init() throws Exception {
    this.captureNext();
  }

  public boolean isStateVisible(StatePredictionPrefix state) {
    return this.currentPredictions.stream()
        .anyMatch(it -> it.prediction().name().startsWith(state.getPrefix()));
  }

  public boolean hasPrediction(Prediction prediction) {
    return this.currentPredictions.stream().anyMatch(it -> it.prediction().equals(prediction));
  }

  public Optional<BoxPrediction> getPrediction(Prediction prediction) {
    return this.currentPredictions.stream()
        .filter(it -> it.prediction().equals(prediction))
        .findFirst();
  }

  public boolean containsImage(
      BufferedImage other,
      double threshold,
      int box_x_min,
      int box_x_max,
      int box_y_min,
      int box_y_max) {
    final var difference = this.difference(other, box_x_min, box_x_max, box_y_min, box_y_max);
    return difference <= threshold;
  }

  public double difference(
      BufferedImage other, int box_x_min, int box_x_max, int box_y_min, int box_y_max) {
    return ImageMatcher.difference(
        this.currentImage, other, box_x_min, box_x_max, box_y_min, box_y_max);
  }

  public String read(int box_x_min, int box_x_max, int box_y_min, int box_y_max) throws Exception {
    return this.reader.read(this.currentImage, box_x_min, box_x_max, box_y_min, box_y_max);
  }

  public boolean isColorAt(Pixel pixel, int red, int green, int blue, int tolerance) {
    final var rgbPixel = this.currentImage.getRGB(pixel.x(), pixel.y());
    final var imgRed = (rgbPixel >> 16) & 0xFF;
    final var imgGreen = (rgbPixel >> 8) & 0xFF;
    final var imgBlue = rgbPixel & 0xFF;
    return Math.abs(red - imgRed) <= tolerance
        && Math.abs(green - imgGreen) <= tolerance
        && Math.abs(blue - imgBlue) <= tolerance;
  }

  public void captureNext() throws Exception {
    // 120ms
    this.currentImage = this.device.capture();
    // 120ms
    this.currentPredictions = this.yoloPrediction.predict(this.currentImage, CONFIDENCE_THRESHOLDS);
  }

  public void screenshot() throws Exception {
    this.screenshot("screenshot");
  }

  public void screenshot(String path) throws Exception {
    final var image = this.device.capture();
    final var outputfile = new File(path + ".png");
    ImageIO.write(image, "png", outputfile);
  }
}
