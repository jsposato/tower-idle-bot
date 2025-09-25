package de.ttd.ttb.battle;

public record BuyUntilTactic(CashUpgradeOption option, int wave) {
  public static BuyUntilTactic max(CashUpgradeOption cashUpgradeOption) {
    return new BuyUntilTactic(cashUpgradeOption, Integer.MAX_VALUE);
  }

  public static BuyUntilTactic wave(CashUpgradeOption cashUpgradeOption, int wave) {
    return new BuyUntilTactic(cashUpgradeOption, wave);
  }

  public boolean isInvalid(long wave, boolean isMax) {
    return wave > this.wave || isMax;
  }
}
