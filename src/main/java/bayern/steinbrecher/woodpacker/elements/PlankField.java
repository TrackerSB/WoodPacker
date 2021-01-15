package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.Reportable;
import bayern.steinbrecher.checkedElements.report.ReportableBase;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankField extends Control implements Reportable {
    private final StringProperty plankId = new SimpleStringProperty("");
    private final IntegerProperty plankWidth = new SimpleIntegerProperty();
    private final IntegerProperty plankHeight = new SimpleIntegerProperty();
    private final ObjectProperty<PlankGrainDirection> grainDirection = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<PlankMaterial> material = new ReadOnlyObjectWrapper<>(PlankMaterial.UNDEFINED);
    private final ObjectProperty<PlankMaterial> selectedMaterial = new SimpleObjectProperty<>(PlankMaterial.UNDEFINED);
    private final BooleanProperty materialAllowed = new SimpleBooleanProperty(true);
    private final StringProperty comment = new SimpleStringProperty("");
    private final BooleanProperty skinElementsValid = new SimpleBooleanProperty(true);
    private final ReportableBase<PlankField> rBase = new ReportableBase<>(this);

    public PlankField() {
        ChangeListener<Boolean> onMaterialAllowedChanged = (obs, materialWasAllowed, materialIsAllowed) -> {
            if (materialIsAllowed) {
                material.bind(selectedMaterialProperty());
            } else {
                material.unbind();
                material.set(PlankMaterial.UNDEFINED);
            }
        };
        materialAllowedProperty()
                .addListener(onMaterialAllowedChanged);
        onMaterialAllowedChanged.changed(null, null, isMaterialAllowed()); // Ensure initial state
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlankFieldSkin(this);
    }

    public Plank createPlank() {
        return new Plank(
                getPlankId(), getPlankWidth(), getPlankHeight(), getGrainDirection(), getMaterial(), getComment());
    }

    // FIXME Only skins for PlankFields should be allowed to call this method
    void bindSkinElementsValidProperty(ObservableValue<Boolean> validity) {
        skinElementsValid.bind(validity);
    }

    public StringProperty plankIdProperty() {
        return plankId;
    }

    public String getPlankId() {
        return plankIdProperty().get();
    }

    public void setPlankId(String plankId) {
        plankIdProperty().set(plankId);
    }

    public IntegerProperty plankWidthProperty() {
        return plankWidth;
    }

    public int getPlankWidth() {
        return plankWidthProperty().get();
    }

    public void setPlankWidth(int plankWidth) {
        plankWidthProperty().set(plankWidth);
    }

    public IntegerProperty plankHeightProperty() {
        return plankHeight;
    }

    public int getPlankHeight() {
        return plankHeightProperty().get();
    }

    public void setPlankHeight(int plankHeight) {
        plankHeightProperty().set(plankHeight);
    }

    public ObjectProperty<PlankGrainDirection> grainDirectionProperty() {
        return grainDirection;
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirectionProperty().get();
    }

    public void setGrainDirection(PlankGrainDirection direction) {
        grainDirectionProperty().set(direction);
    }

    public ReadOnlyObjectProperty<PlankMaterial> materialProperty() {
        return material.getReadOnlyProperty();
    }

    public PlankMaterial getMaterial() {
        return materialProperty().get();
    }

    public ObjectProperty<PlankMaterial> selectedMaterialProperty() {
        return selectedMaterial;
    }

    public PlankMaterial getSelectedMaterial() {
        return selectedMaterialProperty().get();
    }

    public void setSelectedMaterial(PlankMaterial material) {
        selectedMaterialProperty().set(material);
    }

    public BooleanProperty materialAllowedProperty() {
        return materialAllowed;
    }

    public boolean isMaterialAllowed() {
        return materialAllowedProperty().get();
    }

    public void setMaterialAllowed(boolean materialAllowed) {
        materialAllowedProperty().set(materialAllowed);
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public String getComment() {
        return commentProperty().get();
    }

    public void setComment(String comment) {
        commentProperty().set(comment);
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return rBase.validProperty();
    }

    @Override
    public boolean isValid() {
        return rBase.isValid();
    }

    @Override
    public boolean addValidityConstraint(ObservableBooleanValue constraint) {
        return rBase.addValidityConstraint(constraint);
    }

    @Override
    public ObservableList<ReportEntry> getReports() {
        return rBase.getReports();
    }

    @Override
    public boolean addReport(ReportEntry report) {
        return rBase.addReport(report);
    }
}
