package sh.ball.shapes;

import sh.ball.audio.FrameSet;
import sh.ball.parser.obj.Listener;

import java.util.List;

public class ShapeFrameSet implements FrameSet<List<Shape>> {

  private final List<Shape> shapes;

  public ShapeFrameSet(List<Shape> shapes) {
    this.shapes = shapes;
  }

  @Override
  public List<Shape> next() {
    return shapes;
  }

  @Override
  public void setFrameSettings(Object settings) {}

  @Override
  public void addListener(Listener listener) { }
}
