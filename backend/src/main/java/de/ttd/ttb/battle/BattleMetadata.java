package de.ttd.ttb.battle;

import de.ttd.ttb.capture.CaptureService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BattleMetadata {
  private final CaptureService captureService;

  @Getter private long currentWave = 0;

  public void update() {
    this.readCurrentWave();
  }

  private void readCurrentWave() {
    try {
      final var waveText = this.captureService.read(466, 545, 688, 720);
      this.currentWave = Long.parseLong(waveText.replaceAll("[^\\d.]", ""));
    } catch (Exception e) {
    }
  }
}
