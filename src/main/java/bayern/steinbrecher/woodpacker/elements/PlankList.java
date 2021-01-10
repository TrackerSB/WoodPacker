package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.Plank;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Optional;

public class PlankList extends Control {
    private final SetProperty<Plank> planks = new SimpleSetProperty<>(FXCollections.observableSet());
    private final ReadOnlyObjectWrapper<Optional<Plank>> selectedPlank = new ReadOnlyObjectWrapper<>(Optional.empty());
    private final ReadOnlyBooleanWrapper plankSelected = new ReadOnlyBooleanWrapper(false);

    public PlankList() {
        selectedPlank.addListener((obs, previousPlank, currentPlank) -> plankSelected.set(currentPlank.isPresent()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlankListSkin(this);
    }

    public SetProperty<Plank> planksProperty() {
        return planks;
    }

    public ObservableSet<Plank> getPlanks() {
        return planksProperty().get();
    }

    public void setPlanks(ObservableSet<Plank> planks) {
        planksProperty().set(planks);
    }

    public ReadOnlyObjectProperty<Optional<Plank>> selectedPlankProperty() {
        return selectedPlank.getReadOnlyProperty();
    }

    public Optional<Plank> getSelectedPlank() {
        return selectedPlankProperty().get();
    }

    // NOTE Only Skins should be allowed to call this method
    void setSelectedPlank(Plank selectedPlank) {
        this.selectedPlank.set(Optional.ofNullable(selectedPlank));
    }

    public ReadOnlyBooleanProperty plankSelectedProperty() {
        return plankSelected;
    }

    public boolean isPlankSelected() {
        return plankSelectedProperty().get();
    }
}
