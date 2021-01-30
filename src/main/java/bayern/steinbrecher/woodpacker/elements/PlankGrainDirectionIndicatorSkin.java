package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
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

    private static final Map<PlankGrainDirection, String> GRAIN_DIRECTION_SYMBOL_FILE_NAMES = Map.of(
            PlankGrainDirection.IRRELEVANT, "plankGrainIrrelevant.png",
            PlankGrainDirection.HORIZONTAL, "plankGrainHorizontal.png",
            PlankGrainDirection.VERTICAL, "plankGrainVertical.png"
    );

    protected PlankGrainDirectionIndicatorSkin(final PlankGrainDirectionIndicator control) {
        super(control);

        Button indicatorButton = new Button();
        indicatorButton.setOnAction(aevt -> {
            control.setValue(
                    switch (control.getValue()) {
                        case HORIZONTAL -> PlankGrainDirection.IRRELEVANT;
                        case IRRELEVANT -> PlankGrainDirection.VERTICAL;
                        case VERTICAL -> PlankGrainDirection.HORIZONTAL;
                    }
            );
        });

        Consumer<PlankGrainDirection> updateIndicatorGraphic = direction -> {
            indicatorButton.setGraphic(generateImageView(direction));
        };
        control.valueProperty()
                .addListener((obs, previousDirection, currentDirection) -> {
                    updateIndicatorGraphic.accept(currentDirection);
                });
        updateIndicatorGraphic.accept(control.getValue()); // Ensure initial state

        getChildren()
                .add(indicatorButton);
    }

    public static ImageView generateImageView(final PlankGrainDirection direction) {
        String imageName = GRAIN_DIRECTION_SYMBOL_FILE_NAMES.get(direction);
        URL symbolResource = PlankGrainDirectionIndicatorSkin.class
                .getResource(imageName);
        if (symbolResource == null) {
            throw new ExceptionInInitializerError(String.format("Cannot find symbol for '%s'", imageName));
        } else {
            return new ImageView(symbolResource.toExternalForm());
        }
    }
}
