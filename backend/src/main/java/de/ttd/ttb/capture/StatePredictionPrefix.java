package de.ttd.ttb.capture;

public enum StatePredictionPrefix {
  Battle("B"),
  Overview("O"),
  GemAd("GA");

  private final String prefix;

  StatePredictionPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getPrefix() {
    return this.prefix;
  }
}
