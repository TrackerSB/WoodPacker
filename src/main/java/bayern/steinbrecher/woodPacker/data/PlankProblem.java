package bayern.steinbrecher.woodPacker.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankProblem {
    private final ListProperty<Plank> requiredPlanks = new SimpleListProperty<>(null);
    private final ObjectProperty<Plank> basePlank = new SimpleObjectProperty<>(null);
    private final ReadOnlyObjectWrapper<Optional<List<Pair<Plank, Point2D>>>> proposedSolution
            = new ReadOnlyObjectWrapper<>(Optional.empty());

    public PlankProblem() {
        requiredPlanksProperty()
                .addListener((obs, oldList, currentList) -> {
                    if (currentList != null) {
                        currentList.addListener((InvalidationListener) observable -> {
                            // FIXME Use observable instead of requiredPlanks?
                            proposedSolution.set(determineSolution(getBasePlank(), getRequiredPlanks()));
                        });
                    }
                });
        requiredPlanksProperty()
                .set(FXCollections.observableArrayList()); // Ensure initial state
        basePlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank) -> {
                    proposedSolution.set(determineSolution(currentBasePlank, getRequiredPlanks()));
                });
    }

    private static Optional<List<Pair<Plank, Point2D>>> determineSolution(Plank basePlank, List<Plank> requiredPlanks) {
        return Optional.empty();
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

    public ObjectProperty<Plank> basePlankProperty() {
        return basePlank;
    }

    public Plank getBasePlank() {
        return basePlankProperty().get();
    }

    public void setBasePlank(Plank basePlank) {
        basePlankProperty().set(basePlank);
    }

    public ReadOnlyObjectProperty<Optional<List<Pair<Plank, Point2D>>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Optional<List<Pair<Plank, Point2D>>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }
}
