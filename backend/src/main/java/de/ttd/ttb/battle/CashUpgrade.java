package de.ttd.ttb.battle;

import lombok.Getter;

@Getter
public class CashUpgrade {
  // private static

  private final CashUpgradeOption cashUpgradeOption;
  private boolean isMax;
  private boolean isTooExpensive;

  public CashUpgrade(CashUpgradeOption cashUpgradeOption) {
    this(cashUpgradeOption, false, false);
  }

  public CashUpgrade(CashUpgradeOption cashUpgradeOption, boolean isMax, boolean isTooExpensive) {
    this.cashUpgradeOption = cashUpgradeOption;
    this.isMax = isMax;
    this.isTooExpensive = isTooExpensive;
  }

  public void update(boolean isMax, boolean isTooExpensive) {
    this.isMax = isMax;
    this.isTooExpensive = isTooExpensive;
  }

  public boolean isBuyable() {
    return !isMax && !isTooExpensive;
  }
}
