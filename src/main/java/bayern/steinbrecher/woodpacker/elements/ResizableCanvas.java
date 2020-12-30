package bayern.steinbrecher.woodpacker.elements;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Prototype for a canvas which resizes with its parent.
 *
 * @author Stefan Huber
 * @see https://stackoverflow.com/a/31761362/230513
 * @see https://stackoverflow.com/a/8616169/230513
 * @since 0.1
 * @deprecated This class has not the functionality it was supposed to have and therefore is unused.
 */
@Deprecated(since = "0.1")
public class ResizableCanvas extends Canvas {

    public ResizableCanvas() {
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    private void draw() {
        double width = getWidth();
        double height = getHeight();

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        gc.setStroke(Color.RED);
        gc.strokeLine(0, 0, width, height);
        gc.strokeLine(0, height, width, 0);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}
