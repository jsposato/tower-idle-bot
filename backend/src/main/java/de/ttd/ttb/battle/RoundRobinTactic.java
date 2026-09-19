package de.ttd.ttb.battle;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;

@Getter
public class RoundRobinTactic {
  private final List<BuyUntilTactic> tactics;
  private int currentTactic;

  public RoundRobinTactic(final List<BuyUntilTactic> tactics) {
    this.tactics = tactics;
    this.currentTactic = 0;
  }

  public boolean isValid(long wave, Function<CashUpgradeOption, Boolean> isMax) {
    return this.tactics.stream().allMatch(it -> it.isInvalid(wave, isMax.apply(it.option())));
  }

  public CashUpgradeOption next(long wave, Function<CashUpgradeOption, Boolean> isMax) {
    var current = this.tactics.get(this.currentTactic);
    while (current.isInvalid(wave, isMax.apply(current.option()))) {
      this.currentTactic = (this.currentTactic + 1) % this.tactics.size();
      current = this.tactics.get(this.currentTactic);
    }
    this.currentTactic = (this.currentTactic + 1) % this.tactics.size();
    return current.option();
  }
}
