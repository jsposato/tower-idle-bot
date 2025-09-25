package de.ttd.ttb.state;

import de.ttd.ttb.Device;
import de.ttd.ttb.capture.CaptureService;
import de.ttd.ttb.capture.StatePredictionPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverviewGS extends GameState {
  private final Device device;
  private final CaptureService captureService;

  private void battle() throws Exception {
    System.out.println("Starting battle");
    this.device.tap(320, 1100);
    Thread.sleep(500);
    this.captureService.captureNext();
  }

  @Override
  public void next() throws Exception {
    this.battle();
  }

  @Override
  public boolean isVisible() {
    return captureService.isStateVisible(StatePredictionPrefix.Overview);
  }
}
