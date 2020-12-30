package bayern.steinbrecher.woodpacker.data;

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
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankProblem {
    private final ListProperty<Plank> requiredPlanks = new SimpleListProperty<>(null);
    private final ObjectProperty<Plank> basePlank = new SimpleObjectProperty<>(null);
    private final ReadOnlyObjectWrapper<Pair<List<PlankSolutionRow>, List<Plank>>> proposedSolution
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
    private static Pair<List<PlankSolutionRow>, List<Plank>> determineSolution(
            Plank basePlank, List<Plank> requiredPlanks) {
        List<PlankSolutionRow> placedPlanks = new ArrayList<>();
        List<Plank> ignoredPlanks;
        if (basePlank == null) {
            ignoredPlanks = requiredPlanks;
        } else {
            ignoredPlanks = new ArrayList<>();
            List<Plank> planksToPlace = requiredPlanks.stream()
                    .map(plank -> plank.matchesGrainDirection(basePlank.getGrainDirection()) ? plank : plank.rotated())
                    .sorted((p1, p2) -> p2.getHeight() - p1.getHeight())
                    .collect(Collectors.toList());
            double heightOfAddedRows = 0;
            for (Plank plank : planksToPlace) {
                boolean placedInExistingRow = false;
                for (PlankSolutionRow row : placedPlanks) {
                    if (row.addPlank(plank)) {
                        placedInExistingRow = true;
                        break;
                    }
                }
                if (!placedInExistingRow) {
                    double expectedEndY = heightOfAddedRows + plank.getHeight();
                    if (expectedEndY <= basePlank.getHeight()) {
                        PlankSolutionRow newRow = new PlankSolutionRow(heightOfAddedRows, plank.getHeight(),
                                basePlank.getWidth(), basePlank.getGrainDirection());
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

    public ReadOnlyObjectProperty<Pair<List<PlankSolutionRow>, List<Plank>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Pair<List<PlankSolutionRow>, List<Plank>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }
}
