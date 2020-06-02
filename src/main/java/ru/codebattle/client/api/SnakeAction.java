package ru.codebattle.client.api;

public class SnakeAction {
  private static final String ACT_COMMAND_PREFIX = "ACT,";
  private static final String ACT_COMMAND_POSTFIX = ",ACT";

  private final boolean preAct;
  private final boolean postAct;
  private final Direction direction;

  public boolean isAct() {
    return preAct || postAct;
  }

  public Direction getDirection() {
    return direction;
  }

  private SnakeAction() {
    preAct = true;
    postAct = true;
    direction = null;
  }

  public static SnakeAction suicide() {
    return new SnakeAction();
  }

  public static SnakeAction preAct(Direction direction) {
    return new SnakeAction(true, direction);
  }

  public static SnakeAction postAct(Direction direction) {
    return new SnakeAction(direction, true);
  }

  public SnakeAction(boolean preAct, Direction direction) {
    this.preAct = preAct;
    this.direction = direction;
    postAct = false;
  }

  public SnakeAction(Direction direction) {
    this.direction = direction;
    preAct = false;
    postAct = false;
  }

  public SnakeAction(Direction direction, boolean postAct) {
    this.preAct = false;
    this.direction = direction;
    this.postAct = postAct;
  }

  @Override
  public String toString() {
    if (direction == null) {
      return "ACT(0)";
    }
    String pre = preAct ? ACT_COMMAND_PREFIX : "";
    String post = postAct ? ACT_COMMAND_POSTFIX : "";
    return pre + direction.toString() + post;
  }
}
