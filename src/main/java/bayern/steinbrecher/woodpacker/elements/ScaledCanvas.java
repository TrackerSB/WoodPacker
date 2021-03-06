package bayern.steinbrecher.woodpacker.elements;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * Provides a {@link Canvas} which scales its size to fit into an area of given width and height while keeping the
 * aspect ratio of its drawings. This behavior is archived by distinguishing the expected size of the component
 * (see {@link #getWidth()} and {@link #getHeight()}) and the size the {@link Canvas} would have had if the fit area was
 * as big as the {@link Canvas} (see {@link #getTheoreticalWidth()} and {@link #getTheoreticalHeight()}). This
 * separation allows to easily calculate a factor by which every {@link #getDrawingActions()} has to be scaled before
 * applying it to the {@link Canvas}. Since this strategy completely avoids the methods {@link #getScaleX()} and
 * {@link #getScaleY()} the resulting visualization will never be blurry or pixelated.
 *
 * @author Stefan Huber
 * @since 0.1
 */
public class ScaledCanvas extends Region {
    private final DoubleProperty theoreticalWidth = new SimpleDoubleProperty();
    private final DoubleProperty theoreticalHeight = new SimpleDoubleProperty();
    private final ObjectProperty<Consumer<GraphicsContext>> drawingActions = new SimpleObjectProperty<>();
    private final Canvas drawingArea = new Canvas();

    public ScaledCanvas() {
        super();

        // Fit content holder to expected user defined dimensions
        final StackPane contentHolder = new StackPane(drawingArea);
        contentHolder.minWidthProperty()
                .bind(minWidthProperty());
        contentHolder.minHeightProperty()
                .bind(minHeightProperty());
        contentHolder.prefWidthProperty()
                .bind(widthProperty());
        contentHolder.prefHeightProperty()
                .bind(heightProperty());
        contentHolder.maxWidthProperty()
                .bind(maxWidthProperty());
        contentHolder.maxHeightProperty()
                .bind(maxHeightProperty());

        // TODO Determine why setting min width and height explicitly is required in order to make auto resize work
        setMinWidth(300);
        setMinHeight(300);

        final NumberBinding theoreticalToActualDrawingAreaFactor = Bindings.min(
                contentHolder.widthProperty().divide(theoreticalWidth),
                contentHolder.heightProperty().divide(theoreticalHeight)
        );

        // Fit drawing area to content holder but keeping the aspect ratio
        drawingArea.widthProperty()
                .bind(theoreticalWidthProperty().multiply(theoreticalToActualDrawingAreaFactor));
        drawingArea.heightProperty()
                .bind(theoreticalHeightProperty().multiply(theoreticalToActualDrawingAreaFactor));

        getChildren()
                .add(contentHolder);

        // Trigger redraw
        final Runnable redraw = () -> {
            GraphicsContext context = drawingArea.getGraphicsContext2D();
            context.save(); // Ensure that the set scales do not stack on each redraw
            context.setFill(Color.WHITE);
            context.fillRect(0, 0, drawingArea.getWidth(), drawingArea.getHeight());
            context.scale(theoreticalToActualDrawingAreaFactor.doubleValue(),
                    theoreticalToActualDrawingAreaFactor.doubleValue());
            if (getDrawingActions() != null) {
                getDrawingActions()
                        .accept(context);
            }
            context.restore();
        };
        contentHolder.widthProperty().addListener(observable -> redraw.run());
        contentHolder.heightProperty().addListener(observable -> redraw.run());
        theoreticalWidthProperty().addListener(observable -> redraw.run());
        theoreticalHeightProperty().addListener(observable -> redraw.run());
        theoreticalToActualDrawingAreaFactor.addListener(observable -> redraw.run());
        drawingActionsProperty().addListener(observable -> redraw.run());
    }

    public WritableImage snapshotDrawingArea() {
        return drawingArea.snapshot(new SnapshotParameters(), null);
    }

    public DoubleProperty theoreticalWidthProperty() {
        return theoreticalWidth;
    }

    public double getTheoreticalWidth() {
        return theoreticalWidthProperty().get();
    }

    public void setTheoreticalWidth(final double theoreticalWidth) {
        theoreticalWidthProperty().set(theoreticalWidth);
    }

    public DoubleProperty theoreticalHeightProperty() {
        return theoreticalHeight;
    }

    public double getTheoreticalHeight() {
        return theoreticalHeightProperty().get();
    }

    public void setTheoreticalHeight(final double theoreticalHeight) {
        theoreticalHeightProperty().set(theoreticalHeight);
    }

    public ObjectProperty<Consumer<GraphicsContext>> drawingActionsProperty() {
        return drawingActions;
    }

    public Consumer<GraphicsContext> getDrawingActions() {
        return drawingActionsProperty().get();
    }

    public void setDrawingActions(final Consumer<GraphicsContext> drawingActions) {
        drawingActionsProperty().set(drawingActions);
    }
}
