package bayern.steinbrecher.woodPacker.data;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class PlankProblem {
    private final ListProperty<Plank> requiredPlanks = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ObjectProperty<Plank> basePlank = new SimpleObjectProperty<>(null);
    private final ReadOnlyObjectWrapper<Optional<List<Plank>>> proposedSolution
            = new ReadOnlyObjectWrapper<>(Optional.empty());

    public PlankProblem() {
    }

    public ListProperty<Plank> requiredPlanksProperty() {
        return requiredPlanks;
    }

    public ObservableList<Plank> getRequiredPlanks() {
        return requiredPlanksProperty().get();
    }

    public void setRequiredPlanks(ObservableList<Plank> requiredPlanks) {
        requiredPlanksProperty().set(requiredPlanks);
    }

    public void addRequiredPlank(Plank requiredPlank) {
        requiredPlanksProperty().add(requiredPlank);
    }

    public ObjectProperty<Plank> basePlankProperty() {
        return basePlank;
    }

    public Plank getBasePlank() {
        return basePlankProperty().get();
    }

    public void setBasePlank(Plank basePlank) {
        basePlankProperty().set(basePlank);
    }

    public ReadOnlyObjectProperty<Optional<List<Plank>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Optional<List<Plank>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }
}
