package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankGrainDirectionIndicatorSkin extends SkinBase<PlankGrainDirectionIndicator> {

    private static final Map<PlankGrainDirection, String> GRAIN_DIRECTION_SYMBOL_FILE_NAMES = Map.of(
            PlankGrainDirection.IRRELEVANT, "plankGrainIrrelevant.png",
            PlankGrainDirection.HORIZONTAL, "plankGrainHorizontal.png",
            PlankGrainDirection.VERTICAL, "plankGrainVertical.png"
    );

    private Button createIndicatorButton(final PlankGrainDirectionIndicator control) {
        final Button indicatorButton = new Button();
        indicatorButton.setOnAction(
                aevt -> control.setValue(
                        switch (control.getValue()) {
                            case HORIZONTAL -> PlankGrainDirection.IRRELEVANT;
                            case IRRELEVANT -> PlankGrainDirection.VERTICAL;
                            case VERTICAL -> PlankGrainDirection.HORIZONTAL;
                        }
                ));

        final Consumer<PlankGrainDirection> updateIndicatorGraphic
                = direction -> indicatorButton.setGraphic(generateImageView(direction));
        control.valueProperty()
                .addListener((obs, previousDirection, currentDirection)
                        -> updateIndicatorGraphic.accept(currentDirection));
        updateIndicatorGraphic.accept(control.getValue()); // Ensure initial state
        return indicatorButton;
    }

    private Node createAutoModeOverlay(final PlankGrainDirectionIndicator control, final Region indicatorButton) {
        final Rectangle autoStateBackground = new Rectangle();
        autoStateBackground.setFill(Color.rgb(255, 255, 255, 0.5));
        autoStateBackground.widthProperty()
                .bind(indicatorButton.widthProperty());
        autoStateBackground.heightProperty()
                .bind(indicatorButton.heightProperty());
        autoStateBackground.visibleProperty()
                .bind(control.inAutoModeProperty());

        final Text autoStateText = new Text("A");
        autoStateText.visibleProperty()
                .bind(control.inAutoModeProperty());

        final PlankField<?> autoConnection = control.getAutoConnection();
        if (autoConnection != null) {
            final Runnable updateAutoValue = () -> {
                if (control.isInAutoMode()) {
                    control.setValue(
                            autoConnection.getPlankHeight() > autoConnection.getPlankWidth()
                                    ? PlankGrainDirection.VERTICAL
                                    : PlankGrainDirection.HORIZONTAL);
                }
            };
            control.inAutoModeProperty()
                    .addListener((obs, wasInAutoMode, isInAutoMode) -> updateAutoValue.run());
            autoConnection.plankWidthProperty()
                    .addListener((obs, previousWidth, currentWidth) -> updateAutoValue.run());
            autoConnection.plankHeightProperty()
                    .addListener((obs, previousHeight, currentHeight) -> updateAutoValue.run());
        }

        return new StackPane(autoStateBackground, autoStateText);
    }

    protected PlankGrainDirectionIndicatorSkin(final PlankGrainDirectionIndicator control) {
        super(control);

        final Button indicatorButton = createIndicatorButton(control);
        final Node autoModeOverlay = createAutoModeOverlay(control, indicatorButton);

        // Handle mouse events controlling the value and the auto mode state
        final StackPane indicatorNode = new StackPane(indicatorButton, autoModeOverlay);
        indicatorNode.setOnMouseClicked(mevt -> {
            switch (mevt.getButton()) {
            case PRIMARY -> {
                control.setValueUserDefined(true);
                // indicatorButton.fireEvent(mevt); // FIXME This event does not reach the underlying button
                indicatorButton.getOnAction()
                        .handle(null);
            }
            case SECONDARY -> control.setValueUserDefined(false);
            default -> {
                // No-op
            }
            }
        });

        getChildren()
                .add(indicatorNode);
    }

    public static ImageView generateImageView(final PlankGrainDirection direction) {
        final String imageName = GRAIN_DIRECTION_SYMBOL_FILE_NAMES.get(direction);
        final URL symbolResource = PlankGrainDirectionIndicatorSkin.class
                .getResource(imageName);
        if (symbolResource == null) {
            throw new ExceptionInInitializerError(String.format("Cannot find symbol for '%s'", imageName));
        } else {
            return new ImageView(symbolResource.toExternalForm());
        }
    }
}
