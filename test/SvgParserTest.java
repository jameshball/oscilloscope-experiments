import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.xml.sax.SAXException;
import parser.SvgParser;
import shapes.Line;
import shapes.Shape;

public class SvgParserTest {

  private List<? extends Shape> getShapes(SvgParser parser) {
    return parser.getShapes().get(0);
  }

  @Test
  public void lineToGeneratesALineShape()
      throws ParserConfigurationException, SAXException, IOException {
    SvgParser svgParser = new SvgParser("test/images/line-to.svg");
    assertEquals(getShapes(svgParser), Line.pathToLines(0.5, 0.5, 0.75, 1, 0, 0, 0.5, 0.5));
  }

  @Test
  public void horizontalLineToGeneratesAHorizontalLineShape()
      throws ParserConfigurationException, SAXException, IOException {
    SvgParser svgParser = new SvgParser("test/images/horizontal-line-to.svg");
    assertEquals(getShapes(svgParser), Line.pathToLines(0.5, 0.5, 0.75, 0.5, 0, 0.5, 0.5, 0.5));
  }

  @Test
  public void verticalLineToGeneratesAVerticalLineShape()
      throws ParserConfigurationException, SAXException, IOException {
    SvgParser svgParser = new SvgParser("test/images/vertical-line-to.svg");
    assertEquals(getShapes(svgParser), Line.pathToLines(0.5, 0.5, 0.5, 0.75, 0.5, 0, 0.5, 0.5));
  }

  @Test
  public void closingASubPathDrawsLineToInitialPoint()
      throws ParserConfigurationException, SAXException, IOException {
    SvgParser svgParser = new SvgParser("test/images/closing-subpath.svg");
    assertEquals(getShapes(svgParser), Line.pathToLines(0.5, 0.5, 0.75, 0.5, 0.75, 0.75, 0.5, 0.75, 0.5, 0.5));
  }
}
