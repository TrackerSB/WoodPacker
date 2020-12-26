package bayern.steinbrecher.woodPacker.elements;

import bayern.steinbrecher.woodPacker.data.PlankGrainDirection;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankGrainDirectionIndicatorSkin extends SkinBase<PlankGrainDirectionIndicator> {

    private static final Map<PlankGrainDirection, Node> GRAIN_DIRECTION_SYMBOLS = Map.of(
            PlankGrainDirection.IRRELEVANT, generateImageView("plankGrainIrrelevant.png"),
            PlankGrainDirection.HORIZONTAL, generateImageView("plankGrainHorizontal.png"),
            PlankGrainDirection.VERTICAL, generateImageView("plankGrainVertical.png")
    );
    private final ReadOnlyObjectWrapper<PlankGrainDirection> plankGrainDirection
            = new ReadOnlyObjectWrapper<>(PlankGrainDirection.IRRELEVANT);

    protected PlankGrainDirectionIndicatorSkin(PlankGrainDirectionIndicator control) {
        super(control);

        Button indicatorButton = new Button();
        indicatorButton.setOnAction(aevt -> {
            plankGrainDirection.set(
                    switch (getPlankGrainDirection()) {
                        case HORIZONTAL -> PlankGrainDirection.IRRELEVANT;
                        case IRRELEVANT -> PlankGrainDirection.VERTICAL;
                        case VERTICAL -> PlankGrainDirection.HORIZONTAL;
                    }
            );
        });

        Consumer<PlankGrainDirection> updateIndicatorGraphic = direction -> {
            indicatorButton.setGraphic(GRAIN_DIRECTION_SYMBOLS.get(direction));
        };
        plankGrainDirectionProperty()
                .addListener((obs, previousDirection, currentDirection) -> {
                    updateIndicatorGraphic.accept(currentDirection);
                });
        updateIndicatorGraphic.accept(getPlankGrainDirection()); // Ensure initial state

        getChildren()
                .add(indicatorButton);
    }

    private static ImageView generateImageView(String imageName) {
        URL symbolResource = PlankGrainDirectionIndicatorSkin.class
                .getResource(imageName);
        if (symbolResource == null) {
            throw new ExceptionInInitializerError(String.format("Cannot find symbol for '%s'", imageName));
        } else {
            return new ImageView(symbolResource.toExternalForm());
        }
    }

    public ReadOnlyObjectProperty<PlankGrainDirection> plankGrainDirectionProperty() {
        return plankGrainDirection.getReadOnlyProperty();
    }

    public PlankGrainDirection getPlankGrainDirection() {
        return plankGrainDirectionProperty().get();
    }
}
