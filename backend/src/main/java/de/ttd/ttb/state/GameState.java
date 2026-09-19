package de.ttd.ttb.state;


public abstract class GameState {
  public abstract void next() throws Exception;

  public abstract boolean isVisible();
}
