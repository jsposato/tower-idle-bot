package de.ttd.ttb;

import de.ttd.ttb.capture.CaptureService;
import de.ttd.ttb.state.BattleGS;
import de.ttd.ttb.state.GameState;
import de.ttd.ttb.state.GemAdGS;
import de.ttd.ttb.state.OverviewGS;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Bot implements CommandLineRunner {
  private final Device device;
  private final OverviewGS overviewGS;
  private final BattleGS battleGS;
  private final GemAdGS gemAdGS;
  private final CaptureService captureService;

  private LocalDateTime lastCapture = LocalDateTime.now().minusSeconds(2);

  @Override
  public void run(final String... args) {
    while (true) {
      try {
        if (!this.device.isAppRunning()) {
          System.out.println("App not running, trying to start it...");
          this.device.startApp();
          Thread.sleep(20000);
          continue;
        }

        // Fallback in case its stuck due to some animation
        if (lastCapture.isBefore(LocalDateTime.now())) {
          this.captureService.captureNext();
          this.lastCapture = LocalDateTime.now().plusSeconds(1);
        }

        final var visibleState =
            Stream.of(overviewGS, battleGS, gemAdGS).filter(GameState::isVisible).findFirst();
        if (visibleState.isPresent()) {
          visibleState.get().next();
        }
      } catch (Exception e) {
        // System.out.println("Exception occurred in a state");
        // e.printStackTrace();
      }
    }
  }
}
