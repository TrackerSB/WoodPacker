package bayern.steinbrecher.woodpacker.data;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankProblem {
    /**
     * A criterion is a pair of a function which calculates a value based on a given {@link Plank} and a weight. The
     * higher the resulting value the better a given {@link Plank} suits the criterion.
     */
    private static final Collection<Pair<Function<PlankSolutionRow, Double>, Double>> CRITERIA = List.of(
            // The less the breadths of the planks differs the better
            new Pair<>(row -> 1d / row.getBreadths().size(), 1d),
            // The less space a row wastes the better
            new Pair<>(row -> ((double) row.getCurrentLength()) / row.getMaxLength(), 1d)
    );
    private final SetProperty<Plank> requiredPlanks = new SimpleSetProperty<>(null);
    private final ObjectProperty<Plank> basePlank = new SimpleObjectProperty<>(null);
    private final ReadOnlyObjectWrapper<Pair<List<PlankSolutionRow>, Set<Plank>>> proposedSolution
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
        setRequiredPlanks(FXCollections.observableSet()); // Ensure initial state
        basePlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank)
                        -> proposedSolution.set(determineSolution(currentBasePlank, getRequiredPlanks())));
    }

    private static PlankSolutionRow createCandidate(
            boolean horizontal, Point2D basePlankOffset, Plank basePlank, List<Plank> sortedPlanks) {
        assert !sortedPlanks.isEmpty() : "No planks left for creating a candidate";
        // NOTE Assume planks are sorted by height if horizontal is true; sorted by width otherwise
        int maxLength;
        int breadth;
        if (horizontal) {
            maxLength = basePlank.getWidth();
            breadth = sortedPlanks.get(0)
                    .getHeight();
        } else {
            maxLength = basePlank.getHeight();
            breadth = sortedPlanks.get(0)
                    .getWidth();
        }
        PlankSolutionRow candidate = new PlankSolutionRow(basePlankOffset, horizontal, maxLength, breadth);
        Iterator<Plank> iterator = sortedPlanks.iterator();
        //noinspection StatementWithEmptyBody
        while (iterator.hasNext() && candidate.addPlank(iterator.next())) {
            // NOTE Operation already done by addPlank
        }
        return candidate;
    }

    private static double determineCandidateQuality(PlankSolutionRow candidate) {
        return CRITERIA.stream()
                .mapToDouble(criterion -> criterion.getKey().apply(candidate) * criterion.getValue())
                .sum();
    }

    /**
     * @return A list of rows of planks which can be placed on the base plank and a list of the remaining planks that do
     * not fit onto the base plank (besides the already added ones).
     */
    private static Pair<List<PlankSolutionRow>, Set<Plank>> determineSolution(
            Plank basePlank, Set<Plank> requiredPlanks) {
        List<PlankSolutionRow> placedPlanks = new ArrayList<>();
        Set<Plank> ignoredPlanks;
        if (basePlank == null) {
            ignoredPlanks = requiredPlanks;
        } else {
            Collection<Plank> rotatedPlanks = requiredPlanks.stream()
                    .map(plank -> plank.matchesGrainDirection(basePlank.getGrainDirection()) ? plank : plank.rotated())
                    .collect(Collectors.toList());
            List<Plank> planksToPlaceByHeight = rotatedPlanks.stream()
                    .map(plank -> plank.matchesGrainDirection(basePlank.getGrainDirection()) ? plank : plank.rotated())
                    .sorted((p1, p2) -> p2.getHeight() - p1.getHeight())
                    .collect(Collectors.toList());
            List<Plank> planksToPlaceByWidth = rotatedPlanks.stream()
                    .map(plank -> plank.matchesGrainDirection(basePlank.getGrainDirection()) ? plank : plank.rotated())
                    .sorted((p1, p2) -> p2.getWidth() - p1.getWidth())
                    .collect(Collectors.toList());

            Optional<Plank> remainingBasePlank = Optional.of(basePlank);
            Point2D remainingBasePlankOffset = Point2D.ZERO;
            while (remainingBasePlank.isPresent() && !planksToPlaceByHeight.isEmpty()) {
                PlankSolutionRow horizontalCandidate = createCandidate(
                        true, remainingBasePlankOffset, remainingBasePlank.get(), planksToPlaceByHeight);
                PlankSolutionRow verticalCandidate = createCandidate(
                        false, remainingBasePlankOffset, remainingBasePlank.get(), planksToPlaceByWidth);

                if (horizontalCandidate.getPlanks().isEmpty()
                        && verticalCandidate.getPlanks().isEmpty()) {
                    remainingBasePlank = Optional.empty();
                } else {
                    double horizontalCandidateQuality = determineCandidateQuality(horizontalCandidate);
                    double verticalCandidateQuality = determineCandidateQuality(verticalCandidate);
                    PlankSolutionRow bestCandidate;
                    if (horizontalCandidateQuality > verticalCandidateQuality) {
                        bestCandidate = horizontalCandidate;
                        remainingBasePlank = remainingBasePlank.get()
                                .heightDecreased(bestCandidate.getBreadth());
                        remainingBasePlankOffset = remainingBasePlankOffset.add(0, bestCandidate.getBreadth());
                    } else {
                        bestCandidate = verticalCandidate;
                        remainingBasePlank = remainingBasePlank.get()
                                .widthDecreased(bestCandidate.getBreadth());
                        remainingBasePlankOffset = remainingBasePlankOffset.add(bestCandidate.getBreadth(), 0);
                    }
                    placedPlanks.add(bestCandidate);
                    planksToPlaceByHeight.removeAll(bestCandidate.getPlanks());
                    planksToPlaceByWidth.removeAll(bestCandidate.getPlanks());
                }
            }
            // NOTE Ignored planks may not have same rotation as initially given
            ignoredPlanks = new HashSet<>(planksToPlaceByWidth);
        }
        return new Pair<>(placedPlanks, ignoredPlanks);
    }

    public SetProperty<Plank> requiredPlanksProperty() {
        return requiredPlanks;
    }

    public ObservableSet<Plank> getRequiredPlanks() {
        return requiredPlanksProperty().get();
    }

    public void setRequiredPlanks(ObservableSet<Plank> requiredPlanks) {
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

    public ReadOnlyObjectProperty<Pair<List<PlankSolutionRow>, Set<Plank>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Pair<List<PlankSolutionRow>, Set<Plank>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }
}
