package sh.ball.parser;

import sh.ball.FrameSet;
import sh.ball.shapes.Shape;

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
}
