package bayern.steinbrecher.woodpacker.elements;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;

public class ImageButtonSkin extends SkinBase<ImageButton> {

    public ImageButtonSkin(ImageButton control) {
        super(control);

        StackPane contentHolder = new StackPane();
        Bindings.max(control.widthProperty(), control.heightProperty())
                .addListener((obs, previousSize, currentSize)
                        -> contentHolder.setPrefSize(currentSize.doubleValue(), currentSize.doubleValue()));

        ImageView backgroundImage = new ImageView();
        backgroundImage.setPreserveRatio(true);
        ChangeListener<String> onImageUrlChanged = (obs, previousUrl, currentUrl)
                -> backgroundImage.setImage(new Image(currentUrl));
        control.imageUrlProperty()
                .addListener(onImageUrlChanged);
        onImageUrlChanged.changed(null, null, control.getImageUrl()); // Ensure initial state
        contentHolder.getChildren()
                .add(backgroundImage);

        Region imageOverlay = new Region();
        ChangeListener<Paint> onFillColorChanged = (obs, previousFillColor, currentFillColor)
                -> imageOverlay.setBackground(new Background(new BackgroundFill(currentFillColor, null, null)));
        control.fillColorProperty()
                .addListener(onFillColorChanged);
        onFillColorChanged.changed(null, null, control.getFillColor()); // Ensure initial state
        imageOverlay.onMouseClickedProperty()
                .bind(control.onActionProperty());
        contentHolder.getChildren()
                .add(imageOverlay);

        Label buttonLabel = new Label();
        buttonLabel.textProperty()
                .bind(control.textProperty());
        contentHolder.getChildren()
                .add(buttonLabel);

        getChildren()
                .add(contentHolder);
    }
}
