package bayern.steinbrecher.woodpacker.elements;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class ImageButton extends Control {
    private final StringProperty text = new SimpleStringProperty();
    private final StringProperty imageUrl = new SimpleStringProperty();
    private final ObjectProperty<Paint> fillColor = new SimpleObjectProperty<>(Color.TRANSPARENT);
    private final ObjectProperty<EventHandler<MouseEvent>> onAction = new SimpleObjectProperty<>();

    @Override
    protected Skin<ImageButton> createDefaultSkin() {
        return new ImageButtonSkin(this);
    }

    public StringProperty textProperty() {
        return text;
    }

    public String getText() {
        return textProperty().get();
    }

    public void setText(final String text) {
        textProperty().set(text);
    }

    public StringProperty imageUrlProperty() {
        return imageUrl;
    }

    public String getImageUrl() {
        return imageUrlProperty().get();
    }

    public void setImageUrl(final String imageUrl) {
        imageUrlProperty().set(imageUrl);
    }

    public ObjectProperty<Paint> fillColorProperty() {
        return fillColor;
    }

    public Paint getFillColor() {
        return fillColorProperty().get();
    }

    public void setFillColor(final Paint fillColor) {
        fillColorProperty().set(fillColor);
    }

    public ObjectProperty<EventHandler<MouseEvent>> onActionProperty() {
        return onAction;
    }

    public EventHandler<MouseEvent> getOnAction() {
        return onActionProperty().get();
    }

    public void setOnAction(final EventHandler<MouseEvent> onAction) {
        onActionProperty().set(onAction);
    }
}
