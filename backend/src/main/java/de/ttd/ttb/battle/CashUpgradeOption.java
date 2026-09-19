package de.ttd.ttb.battle;

import de.ttd.ttb.capture.Pixel;

public record CashUpgradeOption(String label, Tab tab, Slot slot, int row) {
  // Attack
  public static final CashUpgradeOption DAMAGE =
      new CashUpgradeOption("Damage", Tab.Attack, Slot.Left, 1);
  public static final CashUpgradeOption ATTACK_SPEED =
      new CashUpgradeOption("Attack Speed", Tab.Attack, Slot.Right, 1);
  public static final CashUpgradeOption CRITICAL_CHANCE =
      new CashUpgradeOption("Critical Chance", Tab.Attack, Slot.Left, 2);
  public static final CashUpgradeOption CRITICAL_FACTOR =
      new CashUpgradeOption("Critical Factor", Tab.Attack, Slot.Right, 2);
  public static final CashUpgradeOption RANGE =
      new CashUpgradeOption("Range", Tab.Attack, Slot.Left, 3);
  public static final CashUpgradeOption DAMAGE_PER_METER =
      new CashUpgradeOption("Damage / Meter", Tab.Attack, Slot.Right, 3);
  public static final CashUpgradeOption MULTISHOT_CHANCE =
      new CashUpgradeOption("Multishot Chance", Tab.Attack, Slot.Left, 4);
  public static final CashUpgradeOption MULTISHOT_TARGETS =
      new CashUpgradeOption("Multishot Targets", Tab.Attack, Slot.Right, 4);
  public static final CashUpgradeOption RAPID_FIRE_CHANCE =
      new CashUpgradeOption("Rapid Fire Chance", Tab.Attack, Slot.Left, 5);
  public static final CashUpgradeOption RAPID_FIRE_DURATION =
      new CashUpgradeOption("Rapid Fire Duration", Tab.Attack, Slot.Right, 5);
  public static final CashUpgradeOption BOUNCE_SHOT_CHANCE =
      new CashUpgradeOption("Bounce Shot Chance", Tab.Attack, Slot.Left, 6);
  public static final CashUpgradeOption BOUNCE_SHOT_TARGETS =
      new CashUpgradeOption("Bounce Shot Targets", Tab.Attack, Slot.Right, 6);
  public static final CashUpgradeOption BOUNCE_SHOT_RANGE =
      new CashUpgradeOption("Bounce Shot Range", Tab.Attack, Slot.Left, 7);
  public static final CashUpgradeOption SUPER_CRIT_CHANCE =
      new CashUpgradeOption("Super Crit Chance", Tab.Attack, Slot.Right, 7);
  public static final CashUpgradeOption SUPER_CRIT_MULTIPLIER =
      new CashUpgradeOption("Super Crit Mult", Tab.Attack, Slot.Left, 8);
  public static final CashUpgradeOption REND_ARMOR_CHANCE =
      new CashUpgradeOption("Rend Armor Chance", Tab.Attack, Slot.Right, 8);
  public static final CashUpgradeOption REND_ARMOR_MULTIPLIER =
      new CashUpgradeOption("Rend Armor Mult", Tab.Attack, Slot.Left, 9);

  // Defense
  public static final CashUpgradeOption HEALTH =
      new CashUpgradeOption("Health", Tab.Defense, Slot.Left, 1);
  public static final CashUpgradeOption HEALTH_REGEN =
      new CashUpgradeOption("Health Regen", Tab.Defense, Slot.Right, 1);
  public static final CashUpgradeOption DEFENSE_PERCENT =
      new CashUpgradeOption("Defense %", Tab.Defense, Slot.Left, 2);
  public static final CashUpgradeOption DEFENSE_ABSOLUTE =
      new CashUpgradeOption("Defense Absolute", Tab.Defense, Slot.Right, 2);
  public static final CashUpgradeOption THORN_DAMAGE =
      new CashUpgradeOption("Thorn Damage", Tab.Defense, Slot.Left, 3);
  public static final CashUpgradeOption LIFESTEAL =
      new CashUpgradeOption("Lifesteal", Tab.Defense, Slot.Right, 3);
  public static final CashUpgradeOption KNOCKBACK_CHANCE =
      new CashUpgradeOption("Knockback Chance", Tab.Defense, Slot.Left, 4);
  public static final CashUpgradeOption KNOCKBACK_FORCE =
      new CashUpgradeOption("Knockback Force", Tab.Defense, Slot.Right, 4);
  public static final CashUpgradeOption ORB_SPEED =
      new CashUpgradeOption("Orb Speed", Tab.Defense, Slot.Left, 5);
  public static final CashUpgradeOption ORBS =
      new CashUpgradeOption("Orbs", Tab.Defense, Slot.Right, 5);
  public static final CashUpgradeOption SHOCKWAVE_SIZE =
      new CashUpgradeOption("Shockwave Size", Tab.Defense, Slot.Left, 6);
  public static final CashUpgradeOption SHOCKWAVE_FREQUENCY =
      new CashUpgradeOption("Shockwave Frequency", Tab.Defense, Slot.Right, 6);
  public static final CashUpgradeOption LAND_MINE_CHANCE =
      new CashUpgradeOption("Land Mine Chance", Tab.Defense, Slot.Left, 7);
  public static final CashUpgradeOption LAND_MINE_DAMAGE =
      new CashUpgradeOption("Land Mine Damage", Tab.Defense, Slot.Right, 7);
  public static final CashUpgradeOption LAND_MINE_RADIUS =
      new CashUpgradeOption("Land Mine Radius", Tab.Defense, Slot.Left, 8);
  public static final CashUpgradeOption DEATH_DEFY =
      new CashUpgradeOption("Death Defy", Tab.Defense, Slot.Right, 8);
  public static final CashUpgradeOption WALL_HEALTH =
      new CashUpgradeOption("Wall Health", Tab.Defense, Slot.Left, 9);
  public static final CashUpgradeOption WALL_REBUILD =
      new CashUpgradeOption("Wall Rebuild", Tab.Defense, Slot.Right, 9);

  // Utility
  public static final CashUpgradeOption CASH_BONUS =
      new CashUpgradeOption("Cash Bonus", Tab.Utility, Slot.Left, 1);
  public static final CashUpgradeOption CASH_PER_WAVE =
      new CashUpgradeOption("Cash / Wave", Tab.Utility, Slot.Right, 1);
  public static final CashUpgradeOption COINS_PER_KILL =
      new CashUpgradeOption("Coins / Kill Bonus", Tab.Utility, Slot.Left, 2);
  public static final CashUpgradeOption COINS_PER_WAVE =
      new CashUpgradeOption("Coins / Wave", Tab.Utility, Slot.Right, 2);
  public static final CashUpgradeOption FREE_ATTACK_UPGRADE =
      new CashUpgradeOption("Free Attack Upgrade", Tab.Utility, Slot.Left, 3);
  public static final CashUpgradeOption FREE_DEFENSE_UPGRADE =
      new CashUpgradeOption("Free Defense Upgrade", Tab.Utility, Slot.Right, 3);
  public static final CashUpgradeOption FREE_UTILITY_UPGRADE =
      new CashUpgradeOption("Free Utility Upgrade", Tab.Utility, Slot.Left, 4);
  public static final CashUpgradeOption INTEREST_PER_WAVE =
      new CashUpgradeOption("Interest / Wave", Tab.Utility, Slot.Right, 4);
  public static final CashUpgradeOption RECOVERY_AMOUNT =
      new CashUpgradeOption("Recovery Amount", Tab.Utility, Slot.Left, 5);
  public static final CashUpgradeOption MAX_RECOVERY =
      new CashUpgradeOption("Max Recovery", Tab.Utility, Slot.Right, 5);
  public static final CashUpgradeOption PACKAGE_CHANCE =
      new CashUpgradeOption("Package Chance", Tab.Utility, Slot.Left, 6);
  public static final CashUpgradeOption ENEMY_ATTACK_LEVEL_SKIP =
      new CashUpgradeOption("Enemy Attack Level Skip", Tab.Utility, Slot.Right, 6);
  public static final CashUpgradeOption ENEMY_HEALTH_LEVEL_SKIP =
      new CashUpgradeOption("Enemy Health Level Skip", Tab.Utility, Slot.Left, 7);

  public Pixel getCenter() {
    return new Pixel(this.tab().ordinal() * 3 + this.slot().ordinal(), this.row());
  }
}
