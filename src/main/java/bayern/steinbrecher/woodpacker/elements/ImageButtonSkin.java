package bayern.steinbrecher.woodpacker.elements;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class ImageButtonSkin extends SkinBase<ImageButton> {

    public ImageButtonSkin(ImageButton control) {
        super(control);

        StackPane contentHolder = new StackPane();
        Bindings.max(control.widthProperty(), control.heightProperty())
                .addListener((obs, previousSize, currentSize)
                        -> contentHolder.setPrefSize(currentSize.doubleValue(), currentSize.doubleValue()));
        contentHolder.onMouseClickedProperty()
                .bind(control.onActionProperty());

        Region imageBackground = new Region();
        ChangeListener<Paint> onFillColorChanged = (obs, previousFillColor, currentFillColor)
                -> imageBackground.setBackground(new Background(new BackgroundFill(currentFillColor, null, null)));
        control.fillColorProperty()
                .addListener(onFillColorChanged);
        onFillColorChanged.changed(null, null, control.getFillColor()); // Ensure initial state
        contentHolder.getChildren()
                .add(imageBackground);

        ImageView backgroundImage = new ImageView();
        backgroundImage.setPreserveRatio(true);
        ChangeListener<String> onImageUrlChanged = (obs, previousUrl, currentUrl)
                -> backgroundImage.setImage(new Image(currentUrl));
        control.imageUrlProperty()
                .addListener(onImageUrlChanged);
        onImageUrlChanged.changed(null, null, control.getImageUrl()); // Ensure initial state
        contentHolder.getChildren()
                .add(backgroundImage);

        Text buttonText = new Text();
        buttonText.textProperty()
                .bind(control.textProperty());
        buttonText.wrappingWidthProperty()
                .bind(contentHolder.widthProperty());
        buttonText.setTextAlignment(TextAlignment.CENTER);
        buttonText.setStroke(Color.WHITE);
        buttonText.setFill(Color.BLACK);
        buttonText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 26));
        contentHolder.getChildren()
                .add(buttonText);

        getChildren()
                .add(contentHolder);
    }
}
