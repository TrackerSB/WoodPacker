package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.Plank;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Optional;

public class PlankList<T extends Plank> extends Control {
    private final SetProperty<T> planks = new SimpleSetProperty<>(FXCollections.observableSet());
    private final ReadOnlyObjectWrapper<Optional<T>> selectedPlank = new ReadOnlyObjectWrapper<>(Optional.empty());
    private final ReadOnlyBooleanWrapper plankSelected = new ReadOnlyBooleanWrapper(false);
    private final BooleanProperty materialAllowed = new SimpleBooleanProperty(true);
    private final Class<T> genericRuntimeType;

    public PlankList(Class<T> genericRuntimeType) {
        this.genericRuntimeType = genericRuntimeType;
        selectedPlank.addListener((obs, previousPlank, currentPlank) -> plankSelected.set(currentPlank.isPresent()));
    }

    /**
     * Should be used in FXML only.
     */
    public PlankList(@NamedArg("genericRuntimeType") String genericRuntimeTypeName) throws ClassNotFoundException {
        //noinspection unchecked
        this((Class<T>) Class.forName(genericRuntimeTypeName));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlankListSkin<>(this, genericRuntimeType);
    }

    public SetProperty<T> planksProperty() {
        return planks;
    }

    public ObservableSet<T> getPlanks() {
        return planksProperty().get();
    }

    public void setPlanks(ObservableSet<T> planks) {
        planksProperty().set(planks);
    }

    public ReadOnlyObjectProperty<Optional<T>> selectedPlankProperty() {
        return selectedPlank.getReadOnlyProperty();
    }

    public Optional<T> getSelectedPlank() {
        return selectedPlankProperty().get();
    }

    public void setSelectedPlank(T selectedPlank) {
        this.selectedPlank.set(Optional.ofNullable(selectedPlank));
    }

    public ReadOnlyBooleanProperty plankSelectedProperty() {
        return plankSelected;
    }

    public boolean isPlankSelected() {
        return plankSelectedProperty().get();
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
}
