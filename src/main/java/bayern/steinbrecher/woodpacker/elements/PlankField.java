package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.CheckedControl;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.EdgeBand;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Optional;
import java.util.Set;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankField<T extends Plank> extends Control implements CheckedControl {
    private final StringProperty plankId = new SimpleStringProperty("");
    private final ObjectProperty<Optional<Integer>> plankWidth = new SimpleObjectProperty<>(Optional.empty());
    private final ObjectProperty<Optional<Integer>> plankHeight = new SimpleObjectProperty<>(Optional.empty());
    private final ObjectProperty<PlankGrainDirection> grainDirection = new SimpleObjectProperty<>();
    private final BooleanProperty inAutoGrainDirectionMode = new SimpleBooleanProperty();
    private final ReadOnlyObjectWrapper<PlankMaterial> material = new ReadOnlyObjectWrapper<>(PlankMaterial.UNDEFINED);
    private final ObjectProperty<PlankMaterial> selectedMaterial = new SimpleObjectProperty<>(PlankMaterial.UNDEFINED);
    private final StringProperty comment = new SimpleStringProperty("");
    private final SetProperty<EdgeBand> edgeBands = new SimpleSetProperty<>(FXCollections.observableSet());
    private final IntegerProperty edgeBandThickness = new SimpleIntegerProperty();
    private final ReadOnlyBooleanWrapper allFieldsEmpty = new ReadOnlyBooleanWrapper();
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

        allFieldsEmpty.bind(
                plankIdProperty().isEmpty()
                        .and(Bindings.createBooleanBinding(() -> getPlankWidth().isEmpty(), plankWidthProperty()))
                        .and(Bindings.createBooleanBinding(() -> getPlankHeight().isEmpty(), plankHeightProperty()))
                        .and(inAutoGrainDirectionModeProperty())
                        .and(commentProperty().isEmpty())
                        .and(edgeBandsProperty().emptyProperty())
        );
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
        if (getPlankWidth().isEmpty() || getPlankHeight().isEmpty()) {
            throw new IllegalStateException("Cannot create plank since either height or width is not set");
        }

        T createdPlank;
        if (RequiredPlank.class.isAssignableFrom(genericRuntimeType)) {
            //noinspection unchecked
            createdPlank = (T) new RequiredPlank(getPlankId(), getPlankWidth().get(), getPlankHeight().get(),
                    getGrainDirection(), getComment(), getEdgeBands(), getEdgeBandThickness());
        } else if (BasePlank.class.isAssignableFrom(genericRuntimeType)) {
            //noinspection unchecked
            createdPlank = (T) new BasePlank(
                    getPlankId(), getPlankWidth().get(), getPlankHeight().get(), getGrainDirection(), getMaterial(),
                    getComment());
        } else {
            throw new UnsupportedOperationException(
                    String.format("Creating instances for %s is not supported", genericRuntimeType.getCanonicalName()));
        }
        return createdPlank;
    }

    public void reset() {
        setPlankId("");
        setPlankHeight(null);
        setPlankWidth(null);
        setInAutoGrainDirectionMode(true);
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

    public ObjectProperty<Optional<Integer>> plankWidthProperty() {
        return plankWidth;
    }

    public Optional<Integer> getPlankWidth() {
        return plankWidthProperty().get();
    }

    public void setPlankWidth(final Integer plankWidth) {
        plankWidthProperty().set(Optional.ofNullable(plankWidth));
    }

    public ObjectProperty<Optional<Integer>> plankHeightProperty() {
        return plankHeight;
    }

    public Optional<Integer> getPlankHeight() {
        return plankHeightProperty().get();
    }

    public void setPlankHeight(final Integer plankHeight) {
        plankHeightProperty().set(Optional.ofNullable(plankHeight));
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

    public BooleanProperty inAutoGrainDirectionModeProperty() {
        return inAutoGrainDirectionMode;
    }

    public boolean isInAutoGrainDirectionMode() {
        return inAutoGrainDirectionModeProperty().get();
    }

    public void setInAutoGrainDirectionMode(final boolean inAutoGrainDirectionMode) {
        inAutoGrainDirectionModeProperty().set(inAutoGrainDirectionMode);
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

    public IntegerProperty edgeBandThicknessProperty() {
        return edgeBandThickness;
    }

    public int getEdgeBandThickness() {
        return edgeBandThicknessProperty().get();
    }

    public void setEdgeBandThickness(final int edgeBandThickness) {
        edgeBandThicknessProperty().set(edgeBandThickness);
    }

    public ReadOnlyBooleanProperty allFieldsEmptyProperty() {
        return allFieldsEmpty.getReadOnlyProperty();
    }

    public boolean isAllFieldsEmpty() {
        return allFieldsEmptyProperty().get();
    }

    @Override
    public ReadOnlyBooleanProperty checkedProperty() {
        return rBase.checkedProperty();
    }

    @Override
    public boolean isChecked() {
        return checkedProperty().get();
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
