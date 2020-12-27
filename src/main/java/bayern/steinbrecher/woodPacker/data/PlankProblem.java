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
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankProblem {
    private final ListProperty<Plank> requiredPlanks = new SimpleListProperty<>(null);
    private final ObjectProperty<Plank> basePlank = new SimpleObjectProperty<>(null);
    private final ReadOnlyObjectWrapper<Pair<List<PlankRow>, List<Plank>>> proposedSolution
            = new ReadOnlyObjectWrapper<>(null);

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
        setRequiredPlanks(FXCollections.observableArrayList()); // Ensure initial state
        basePlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank) -> {
                    proposedSolution.set(determineSolution(currentBasePlank, getRequiredPlanks()));
                });
    }

    /**
     * @return A list of rows of planks which can be placed on the base plank and a list of the remaining planks that do
     * not fit onto the base plank (besides the already added ones).
     */
    private static Pair<List<PlankRow>, List<Plank>> determineSolution(Plank basePlank, List<Plank> requiredPlanks) {
        List<PlankRow> placedPlanks = new ArrayList<>();
        List<Plank> ignoredPlanks;
        if (basePlank == null) {
            ignoredPlanks = requiredPlanks;
        } else {
            ignoredPlanks = new ArrayList<>();
            // FIXME Rotation based on the required grain is missing
            Queue<Plank> planksToPlace = new PriorityQueue<>((p1, p2) -> p2.getHeight() - p1.getHeight());
            planksToPlace.addAll(requiredPlanks);
            double heightOfAddedRows = 0;
            while (!planksToPlace.isEmpty()) {
                Plank plank = planksToPlace.poll();
                boolean placedInExistingRow = false;
                for (PlankRow row : placedPlanks) {
                    if (row.addPlank(plank)) {
                        placedInExistingRow = true;
                        break;
                    }
                }
                if (!placedInExistingRow) {
                    double expectedEndY = heightOfAddedRows + plank.getHeight();
                    if (expectedEndY <= basePlank.getHeight()) {
                        PlankRow newRow = new PlankRow(heightOfAddedRows, plank.getHeight(), basePlank.getWidth());
                        newRow.addPlank(plank);
                        placedPlanks.add(newRow);
                        heightOfAddedRows = expectedEndY;
                    } else {
                        ignoredPlanks.add(plank);
                    }
                }
            }
        }
        return new Pair<>(placedPlanks, ignoredPlanks);
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

    public ReadOnlyObjectProperty<Pair<List<PlankRow>, List<Plank>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Pair<List<PlankRow>, List<Plank>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }
}
