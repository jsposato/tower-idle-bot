package de.ttd.ttb.state;

import de.ttd.ttb.Device;
import de.ttd.ttb.capture.CaptureService;
import de.ttd.ttb.capture.Prediction;
import de.ttd.ttb.capture.StatePredictionPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GemAdGS extends GameState {
  private final Device device;
  private final CaptureService captureService;

  private boolean isClaimGemsVisible() {
    return captureService.hasPrediction(Prediction.GA_Claim_Gems);
  }

  private boolean isCloseButtonVisible() {
    return captureService.hasPrediction(Prediction.GA_Close_Ad);
  }

  private void claimGems() throws Exception {
    System.out.println("Claiming gems");
    this.device.tap(320, 1100);
    Thread.sleep(500); // To wait for a proper frame again
    this.captureService.captureNext();
  }

  private void close() throws Exception {
    System.out.println("Closing gem ad");
    this.device.tap(677, 45);
    Thread.sleep(1000); // To wait for a proper frame again
    this.captureService.captureNext();
  }

  @Override
  public void next() throws Exception {
    if (this.isCloseButtonVisible()) {
      this.close();
    } else if (this.isClaimGemsVisible()) {
      this.claimGems();
    }
  }

  @Override
  public boolean isVisible() {
    return captureService.isStateVisible(StatePredictionPrefix.GemAd);
  }
}
