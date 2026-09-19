package de.ttd.ttb.state;

import de.ttd.ttb.Device;
import de.ttd.ttb.battle.BattleMetadata;
import de.ttd.ttb.battle.CashUpgradeService;
import de.ttd.ttb.capture.CaptureService;
import de.ttd.ttb.capture.Prediction;
import de.ttd.ttb.capture.StatePredictionPrefix;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BattleGS extends GameState {
  private final Device device;
  private final CaptureService captureService;
  private final CashUpgradeService cashUpgradeService;
  private final BattleMetadata battleMetadata;

  private boolean initialized = false;
  private LocalDateTime gemCd = LocalDateTime.now();

  private void init() throws Exception {
    this.cashUpgradeService.init();
    this.battleMetadata.update();
    this.initialized = true;
  }

  private boolean isPerkAvailableVisible() {
    return captureService.hasPrediction(Prediction.B_New_Perk);
    // && captureService.read(285, 450, 25, 55).equals("New Perk");
  }

  private LocalDateTime openSelectPerkCD = LocalDateTime.now();

  private void openSelectPerk() throws Exception {
    System.out.println("Opening perk selection");
    this.openSelectPerkCD = LocalDateTime.now().plusSeconds(10);
    this.device.tap(320, 35);
    Thread.sleep(500);
    this.captureService.captureNext();
  }

  private boolean isPerkSelectionVisible() {
    return captureService.hasPrediction(Prediction.B_Perk_Window)
        || captureService.hasPrediction(Prediction.B_Choose_Perks)
        || captureService.hasPrediction(Prediction.B_Selected_Perks);
  }

  private static final List<String> sortedSelectablePerks =
      List.of(
          "Perk wave requirement -20.00%",
          "Increase max game speed by +1.00",
          "Enemies damage -50%, but tower damage -50%",
          "Defense percent +4.00%",
          "x1.15 all coins bonuses",
          "Orbs +1",
          "x1.20 Max Health",
          "Free Upgrade Chance for All +5.0%",
          "x1.15 Cash Bonus",
          "Unlock a Random Ultimate Weapon",
          "Golden Tower Bonus x1.5",
          "x1.15 Damage",
          "+1 Wave on Death Wave",
          "Boss Health -70.0%, But Boss Speed +50%",
          "Spotlight Damage Bonus x1.5",
          "Extra Set of Inner Mines",
          "Chain Lightning Damage x2",
          "Chrono Field Duration +5s",
          "Bounce Shot +2",
          "Interest x1.50",
          "x1.15 Defense Absolute",
          "x1.75 Health Regen",
          "Land Mine Damage x3.50",
          "4 More Smart Missiles",
          "Swamp Radius x1.5",
          "Black Hole Duration +12.0s",
          "Enemies Have -50% Health, but Tower Health Regen and Lifesteal -90%",
          "Ranged Enemies Attack Distance Reduced, But Tower Ranged Enemies Damage x3",
          "Enemies Speed -40%, But Enemies Damage x2.5",
          "x12.00 Cash Per Wave, But Enemy Kill Don't Give Cash",
          "Tower Health Regen x8.00, But Tower Max Health -60%",
          "Lifesteal x2.50, But Knockback force -70%",
          "x1.50 Tower Damage, but Bosses Have x8 Health",
          "x1.80 coins, but Tower Max Health -70%");

  private void selectPerk() throws Exception {
    try {
      final var option1 =
          this.captureService
              .read(200, 630, 200, 295)
              .replaceAll("\\n", " ")
              .replaceAll("\\s{2,}", " ")
              .replaceAll("^[\\s\\n]+|[\\s\\n]+$", "");
      final var option2 =
          this.captureService
              .read(200, 630, 330, 430)
              .replaceAll("\\n", " ")
              .replaceAll("\\s{2,}", " ")
              .replaceAll("^[\\s\\n]+|[\\s\\n]+$", "");
      final var option1Index =
          sortedSelectablePerks.indexOf(
              sortedSelectablePerks.stream()
                  .filter(it -> it.toLowerCase().contains(option1.toLowerCase()))
                  .findFirst()
                  .orElse(sortedSelectablePerks.get(sortedSelectablePerks.size() - 1)));
      final var option2Index =
          sortedSelectablePerks.indexOf(
              sortedSelectablePerks.stream()
                  .filter(it -> it.toLowerCase().contains(option2.toLowerCase()))
                  .findFirst()
                  .orElse(sortedSelectablePerks.get(sortedSelectablePerks.size() - 1)));

      System.out.printf(
          "Available perks: %s (%d) and %s (%d) %n", option1, option1Index, option2, option2Index);

      if (option1Index < option2Index) {
        System.out.printf("Select perk %s%n", option1);
        this.device.tap(260, 240);
      } else {
        System.out.printf("Select perk %s%n", option2);
        this.device.tap(260, 380);
      }
      this.device.tap(623, 104);
      Thread.sleep(500);
      this.captureService.captureNext();
    } catch (Exception e) {
      System.out.println("Failed to best.onnxselect perk!");
    }
  }

  private boolean isCoinBonusesVisible() {
    return captureService.hasPrediction(Prediction.B_All_Coin_Bonuses);
  }

  public void closeCoinBonuses() {
    this.device.tap(614, 266);
  }

  private boolean isGameOver() {
    return captureService.hasPrediction(Prediction.B_Game_Over_Retry)
        || captureService.hasPrediction(Prediction.B_Game_Over_Home);
  }

  private void retry() throws Exception {
    System.out.printf("Game Over at wave %d%n", this.battleMetadata.getCurrentWave());
    System.out.println("Retry battle!");
    this.device.tap(210, 975);
    Thread.sleep(250);
    this.captureService.captureNext();
    this.initialized = false;
    Thread.sleep(1000);
  }

  private void collectFloatingGem() {
    captureService
        .getPrediction(Prediction.B_Floating_Gem)
        .ifPresent((boxPrediction) -> this.device.tap(boxPrediction.center()));
  }

  private boolean isFailedToLoadAdVisible() {
    return captureService.hasPrediction(Prediction.B_Failed_To_Load_Ad_Ok);
  }

  private void clickAdFailedOk() throws Exception {
    System.out.println("Ad loading failed, putting collect gem on CD");
    this.gemCd = LocalDateTime.now().plusSeconds(30);
    this.device.tap(312, 758);
    Thread.sleep(250);
    this.captureService.captureNext();
  }

  private boolean isGemVisible() {
    return captureService.hasPrediction(Prediction.B_Gem_Ad);
  }

  private void collectGem() throws Exception {
    System.out.printf("%s: Collecting gem%n", LocalTime.now());
    this.device.tap(48, 616);
    Thread.sleep(250);
    this.captureService.captureNext();
  }

  @Override
  public void next() throws Exception {
    if (this.isGameOver()) {
      this.retry();
    } else {
      if (this.isFailedToLoadAdVisible()) {
        this.clickAdFailedOk();
      } else if (this.isCoinBonusesVisible()) {
        this.closeCoinBonuses();
      } else if (isPerkSelectionVisible()) {
        this.selectPerk();
      } else if (this.openSelectPerkCD.isBefore(LocalDateTime.now()) && isPerkAvailableVisible()) {
        this.openSelectPerk();
      } else if (this.gemCd.isBefore(LocalDateTime.now()) && this.isGemVisible()) {
        this.collectGem();
      } else {
        if (!this.initialized) {
          this.init();
          return;
        }

        this.battleMetadata.update();
        this.cashUpgradeService.tryUpgrade(20);
        this.collectFloatingGem();
      }
    }
  }

  @Override
  public boolean isVisible() {
    final var visible = this.captureService.isStateVisible(StatePredictionPrefix.Battle);

    // If we return from a gem ad, we need to init again to fix the scrolling offset
    if (!visible) {
      this.initialized = false;
    }

    return visible;
  }
}
