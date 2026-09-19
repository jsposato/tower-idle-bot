package de.ttd.ttb.battle;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CashUpgradeStrategy {
  private final RoundRobinTactic tactic;

  public boolean isInvalid(Function<CashUpgradeOption, CashUpgrade> cashUpgrades, long wave) {
    return this.tactic.isValid(wave, (option) -> cashUpgrades.apply(option).isMax());
  }

  public CashUpgradeOption next(Function<CashUpgradeOption, CashUpgrade> cashUpgrades, long wave) {
    return this.tactic.next(wave, (option) -> cashUpgrades.apply(option).isMax());
  }
}
