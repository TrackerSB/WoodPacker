package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.Reportable;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.EdgeBand;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import javafx.beans.NamedArg;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Set;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankField<T extends Plank> extends Control implements Reportable {
    private final StringProperty plankId = new SimpleStringProperty("");
    private final IntegerProperty plankWidth = new SimpleIntegerProperty();
    private final IntegerProperty plankHeight = new SimpleIntegerProperty();
    private final ObjectProperty<PlankGrainDirection> grainDirection = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<PlankMaterial> material = new ReadOnlyObjectWrapper<>(PlankMaterial.UNDEFINED);
    private final ObjectProperty<PlankMaterial> selectedMaterial = new SimpleObjectProperty<>(PlankMaterial.UNDEFINED);
    private final StringProperty comment = new SimpleStringProperty("");
    private final SetProperty<EdgeBand> edgeBands = new SimpleSetProperty<>(FXCollections.observableSet());
    private final CheckableControlBase<PlankField<T>> rBase = new CheckableControlBase<>(this);
    private final Class<T> genericRuntimeType;

    public PlankField(final Class<T> genericRuntimeType) {
        super();

        this.genericRuntimeType = genericRuntimeType;
        if (BasePlank.class.isAssignableFrom(genericRuntimeType)) {
            material.bind(selectedMaterialProperty());
        } else if (RequiredPlank.class.isAssignableFrom(genericRuntimeType)) {
            material.unbind();
            material.set(PlankMaterial.UNDEFINED);
        } else {
            throw new UnsupportedOperationException(
                    String.format("%s does not support %s as generic type", PlankField.class.getCanonicalName(),
                            genericRuntimeType.getCanonicalName()));
        }
    }

    /**
     * Should be used in FXML only.
     */
    public PlankField(@NamedArg("genericRuntimeType") final String genericRuntimeTypeName)
            throws ClassNotFoundException {
        //noinspection unchecked
        this((Class<T>) Class.forName(genericRuntimeTypeName));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlankFieldSkin<T>(this, genericRuntimeType);
    }

    public T createPlank() {
        T createdPlank;
        if (RequiredPlank.class.isAssignableFrom(genericRuntimeType)) {
            //noinspection unchecked
            createdPlank = (T) new RequiredPlank(getPlankId(), getPlankWidth(), getPlankHeight(), getGrainDirection(),
                    getComment(), getEdgeBands());
        } else if (BasePlank.class.isAssignableFrom(genericRuntimeType)) {
            //noinspection unchecked
            createdPlank = (T) new BasePlank(
                    getPlankId(), getPlankWidth(), getPlankHeight(), getGrainDirection(), getMaterial(),
                    getComment());
        } else {
            throw new UnsupportedOperationException(
                    String.format("Creating instances for %s is not supported", genericRuntimeType.getCanonicalName()));
        }
        return createdPlank;
    }

    public void reset() {
        setPlankId("");
        // FIXME Remove width and height field contents
        // FIXME Re-enable grain direction auto mode
        setSelectedMaterial(PlankMaterial.UNDEFINED);
        setComment("");
    }

    public StringProperty plankIdProperty() {
        return plankId;
    }

    public String getPlankId() {
        return plankIdProperty().get();
    }

    public void setPlankId(final String plankId) {
        plankIdProperty().set(plankId);
    }

    public IntegerProperty plankWidthProperty() {
        return plankWidth;
    }

    public int getPlankWidth() {
        return plankWidthProperty().get();
    }

    public void setPlankWidth(final int plankWidth) {
        plankWidthProperty().set(plankWidth);
    }

    public IntegerProperty plankHeightProperty() {
        return plankHeight;
    }

    public int getPlankHeight() {
        return plankHeightProperty().get();
    }

    public void setPlankHeight(final int plankHeight) {
        plankHeightProperty().set(plankHeight);
    }

    public ObjectProperty<PlankGrainDirection> grainDirectionProperty() {
        return grainDirection;
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirectionProperty().get();
    }

    public void setGrainDirection(final PlankGrainDirection direction) {
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

    public void setSelectedMaterial(final PlankMaterial material) {
        selectedMaterialProperty().set(material);
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public String getComment() {
        return commentProperty().get();
    }

    public void setComment(final String comment) {
        commentProperty().set(comment);
    }

    public SetProperty<EdgeBand> edgeBandsProperty() {
        return edgeBands;
    }

    public Set<EdgeBand> getEdgeBands() {
        return edgeBandsProperty().get();
    }

    public void setEdgeBands(final Set<EdgeBand> edgeBands) {
        edgeBandsProperty().get().clear();
        edgeBandsProperty().get().addAll(edgeBands);
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
    public boolean addValidityConstraint(final ObservableBooleanValue constraint) {
        return rBase.addValidityConstraint(constraint);
    }

    @Override
    public ObservableList<ReportEntry> getReports() {
        return rBase.getReports();
    }

    @Override
    public boolean addReport(final ReportEntry report) {
        return rBase.addReport(report);
    }
}
