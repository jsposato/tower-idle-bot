package de.ttd.ttb.battle;

import de.ttd.ttb.Device;
import de.ttd.ttb.capture.CaptureService;
import de.ttd.ttb.capture.Pixel;
import de.ttd.ttb.capture.Prediction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CashUpgradeService {
  static {
    // Blender
    final var offset = 30;
    final var blenderBuilderPhase = 50 - offset;
    strategies =
        List.of(
            new CashUpgradeStrategy(
                new RoundRobinTactic(
                    List.of(
                        BuyUntilTactic.wave(CashUpgradeOption.HEALTH, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.HEALTH, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.HEALTH, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.RECOVERY_AMOUNT, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.MAX_RECOVERY, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.PACKAGE_CHANCE, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.DAMAGE, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.CRITICAL_FACTOR, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.CRITICAL_FACTOR, blenderBuilderPhase),
                        BuyUntilTactic.wave(
                            CashUpgradeOption.BOUNCE_SHOT_TARGETS, blenderBuilderPhase),
                        BuyUntilTactic.wave(
                            CashUpgradeOption.BOUNCE_SHOT_RANGE, blenderBuilderPhase),
                        BuyUntilTactic.wave(
                            CashUpgradeOption.BOUNCE_SHOT_RANGE, blenderBuilderPhase),
                        BuyUntilTactic.wave(
                            CashUpgradeOption.BOUNCE_SHOT_TARGETS, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.CRITICAL_FACTOR, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.CRITICAL_FACTOR, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.DAMAGE, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.PACKAGE_CHANCE, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.MAX_RECOVERY, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.RECOVERY_AMOUNT, blenderBuilderPhase),
                        BuyUntilTactic.wave(CashUpgradeOption.HEALTH, blenderBuilderPhase)))),
            // Maxing
            new CashUpgradeStrategy(
                new RoundRobinTactic(
                    List.of(
                        BuyUntilTactic.max(CashUpgradeOption.HEALTH),
                        BuyUntilTactic.max(CashUpgradeOption.HEALTH),
                        BuyUntilTactic.max(CashUpgradeOption.HEALTH),
                        BuyUntilTactic.wave(CashUpgradeOption.RECOVERY_AMOUNT, 90 - offset),
                        BuyUntilTactic.wave(CashUpgradeOption.MAX_RECOVERY, 90 - offset),
                        BuyUntilTactic.wave(CashUpgradeOption.PACKAGE_CHANCE, 90 - offset),
                        BuyUntilTactic.wave(CashUpgradeOption.DAMAGE, 400),
                        BuyUntilTactic.max(CashUpgradeOption.CRITICAL_FACTOR),
                        BuyUntilTactic.max(CashUpgradeOption.CRITICAL_FACTOR),
                        BuyUntilTactic.max(CashUpgradeOption.DAMAGE_PER_METER)))));
  }

  private final Device device;
  private final CaptureService captureService;
  private final BattleMetadata battleMetadata;

  private final Map<CashUpgradeOption, CashUpgrade> cashUpgrades = new HashMap<>();
  private static final List<CashUpgradeStrategy> strategies;
  private int currentStrategyIndex = 0;
  private CashUpgradeOption currentOption;
  private Tab currentTab = Tab.Attack;
  private final HashMap<Tab, Integer> currentScrollOffset =
      new HashMap<>() {
        {
          put(Tab.Attack, 0);
          put(Tab.Defense, 0);
          put(Tab.Utility, 0);
          put(Tab.UltimateWeapon, 0);
        }
      };

  public void init() throws Exception {
    this.currentTab =
        isAttackTabVisible()
            ? Tab.Attack
            : isDefenseTabVisible()
                ? Tab.Defense
                : isUtilityTabVisible() ? Tab.Utility : Tab.UltimateWeapon;
    this.currentScrollOffset.put(Tab.Attack, 0);
    this.currentScrollOffset.put(Tab.Defense, 0);
    this.currentScrollOffset.put(Tab.Utility, 0);

    for (final var strategy : strategies) {
      for (final var buyTactic : strategy.getTactic().getTactics()) {
        this.cashUpgrades.put(buyTactic.option(), new CashUpgrade(buyTactic.option()));
      }
    }

    this.goToTab(Tab.Attack);
    this.device.swipe(360, 850, 350);
    this.device.swipe(360, 850, 350);
    this.device.swipe(360, 850, 350);
    this.goToTab(Tab.Defense);
    this.device.swipe(360, 850, 350);
    this.device.swipe(360, 850, 350);
    this.device.swipe(360, 850, 350);
    this.goToTab(Tab.Utility);
    this.device.swipe(360, 850, 350);
    this.device.swipe(360, 850, 350);
    this.device.swipe(360, 850, 350);
    this.captureService.captureNext();
  }

  public void tryUpgrade(int upgrades) throws Exception {
    for (int i = 0; i < upgrades; i++) {
      if (!this.tryUpgradeNext()) {
        break;
      }
    }
  }

  public boolean tryUpgradeNext() throws Exception {
    CashUpgradeStrategy strategy = strategies.get(currentStrategyIndex);
    while (strategy.isInvalid(cashUpgrades::get, this.battleMetadata.getCurrentWave())) {
      System.out.printf("Strategy %d invalid, switching to next strategy%n", currentStrategyIndex);
      this.currentStrategyIndex++;
      strategy = strategies.get(currentStrategyIndex);
    }

    if (strategy == null) {
      return false;
    }

    if (currentOption == null) {
      this.currentOption =
          strategies
              .get(currentStrategyIndex)
              .next(cashUpgrades::get, this.battleMetadata.getCurrentWave());
    }

    final var cashUpgrade = this.cashUpgrades.get(this.currentOption);
    this.goToOption(this.currentOption);
    this.updateCashUpgrade(cashUpgrade);
    if (cashUpgrade.isMax()) {
      this.currentOption = null;
      return true;
    }

    if (cashUpgrade.isBuyable()) {
      this.tapOption(this.currentOption);
      this.currentOption = null;
      return true;
    }

    return false;
  }

  public boolean isAnyTabVisible() {
    return this.isAttackTabVisible() || this.isDefenseTabVisible() || this.isUtilityTabVisible();
  }

  private boolean isDefenseTabVisible() {
    return captureService.hasPrediction(Prediction.B_Tab_Defense);
    // return captureService.containsImage(TAB_DEFENSE, 1000, 0, 720, 770, 825);
  }

  private boolean isAttackTabVisible() {
    return captureService.hasPrediction(Prediction.B_Tab_Attack);
    // return captureService.containsImage(TAB_ATTACK, 1000, 0, 720, 770, 825);
  }

  private boolean isUtilityTabVisible() {
    return captureService.hasPrediction(Prediction.B_Tab_Utility);
    // return captureService.containsImage(TAB_UTILITY, 1000, 0, 720, 770, 825);
  }

  private void goToAttackTab() throws Exception {
    if (this.currentTab.equals(Tab.Attack)) {
      return;
    }

    for (int retry = 0; retry < 3; retry++) {
      this.device.tap(90, 1250);
      Thread.sleep(500);
      this.captureService.captureNext();
      if (isAttackTabVisible()) {
        break;
      }
    }

    if (!isAttackTabVisible()) {
      throw new IllegalStateException("Failed to go to attack tab!");
    }

    this.currentTab = Tab.Attack;
  }

  private void goToDefenseTab() throws Exception {
    if (this.currentTab.equals(Tab.Defense)) {
      return;
    }

    for (int retry = 0; retry < 3; retry++) {
      this.device.tap(270, 1250);
      Thread.sleep(500);
      this.captureService.captureNext();
      if (isDefenseTabVisible()) {
        break;
      }
    }

    if (!isDefenseTabVisible()) {
      throw new IllegalStateException("Failed to go to defense tab!");
    }

    this.currentTab = Tab.Defense;
  }

  private void goToUtilityTab() throws Exception {
    if (this.currentTab.equals(Tab.Utility)) {
      return;
    }

    for (int retry = 0; retry < 3; retry++) {
      this.device.tap(450, 1250);
      Thread.sleep(500);
      this.captureService.captureNext();
      if (isUtilityTabVisible()) {
        break;
      }
    }

    if (!isUtilityTabVisible()) {
      throw new IllegalStateException("Failed to go to utility tab!");
    }

    this.currentTab = Tab.Utility;
  }

  private void goToTab(Tab tab) throws Exception {
    switch (tab) {
      case Attack:
        this.goToAttackTab();
        break;
      case Defense:
        this.goToDefenseTab();
        break;
      case Utility:
        this.goToUtilityTab();
        break;
      case UltimateWeapon:
        break;
    }
  }

  private static final int SCROLL_SWIPE_OFFSET = 151;

  private void goToOption(CashUpgradeOption option) throws Exception {
    final var scrollOffset = Math.max(0, SCROLL_SWIPE_OFFSET * (option.row() - 3));
    this.goToTab(option.tab());
    this.scrollTo(option.tab(), scrollOffset);
  }

  private void tapOption(CashUpgradeOption option) throws Exception {
    final var pixel = this.getOptionPixel(option);
    this.goToOption(option);
    this.device.tap(pixel);
  }

  private Pixel getOptionPixel(CashUpgradeOption option) {
    final var x = option.slot().equals(Slot.Left) ? 200 : 545;
    final var y = Math.min(1155, 875 + 140 * (option.row() - 1));
    return new Pixel(x, y);
  }

  private void scrollTo(Tab tab, int offset) throws Exception {
    final var currentOffset = this.currentScrollOffset.get(tab);
    if (offset == currentOffset) {
      return;
    }

    int difference = currentOffset - offset;

    // TODO: This is rather slow, maybe we can take bigger steps when needed
    for (int swipeOffset = 0;
        swipeOffset < Math.abs(difference) / SCROLL_SWIPE_OFFSET;
        swipeOffset++) {
      if (difference < 0) {
        this.device.swipe(360, 1200, -SCROLL_SWIPE_OFFSET);
      } else {
        this.device.swipe(360, 840, SCROLL_SWIPE_OFFSET + 1);
      }
    }
    this.currentScrollOffset.put(tab, offset);
  }

  private void updateCashUpgrade(CashUpgrade cashUpgrade) throws Exception {
    Thread.sleep(50);
    this.captureService.captureNext();
    final var pixel = this.getOptionPixel(cashUpgrade.getCashUpgradeOption());
    final var isMax = this.captureService.isColorAt(pixel, 44, 34, 41, 5);
    final var isTooExpensive = !isMax && this.captureService.isColorAt(pixel, 29, 29, 75, 3);
    cashUpgrade.update(isMax, isTooExpensive);
    // System.out.printf("%s: %s %s%n", cashUpgrade.getCashUpgradeOption().label(), isMax,
    // isTooExpensive);
  }
}
