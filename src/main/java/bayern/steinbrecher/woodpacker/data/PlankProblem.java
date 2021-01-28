package bayern.steinbrecher.woodpacker.data;

import bayern.steinbrecher.javaUtility.SupplyingMap;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankProblem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private transient /*final*/ ObservableMap<PlankSolutionCriterion, Double> criterionWeights;
    private transient /*final*/ SetProperty<RequiredPlank> requiredPlanks;
    private transient /*final*/ ObjectProperty<BasePlank> basePlank;
    private transient /*final*/ ReadOnlyObjectWrapper<Pair<List<PlankSolutionRow>, Set<RequiredPlank>>>
            proposedSolution;

    public PlankProblem() {
        initializeTransientMember();

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
        criterionWeightsProperty()
                .addListener((MapChangeListener<? super PlankSolutionCriterion, ? super Double>) change
                        -> proposedSolution.set(determineSolution(getBasePlank(), getRequiredPlanks())));
    }

    private void initializeTransientMember() {
        criterionWeights = FXCollections.observableMap(new SupplyingMap<>(criterion -> 1d));
        requiredPlanks = new SimpleSetProperty<>(FXCollections.observableSet());
        basePlank = new SimpleObjectProperty<>(null);
        proposedSolution = new ReadOnlyObjectWrapper<>(new Pair<>(List.of(), Set.of()));
    }

    private static PlankSolutionRow createCandidate(
            boolean horizontal, Point2D basePlankOffset, Plank basePlank, List<RequiredPlank> sortedPlanks) {
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
        Iterator<RequiredPlank> iterator = sortedPlanks.iterator();
        //noinspection StatementWithEmptyBody
        while (iterator.hasNext() && candidate.addPlank(iterator.next())) {
            // NOTE Operation already done by addPlank
        }
        return candidate;
    }

    private double determineCandidateQuality(PlankSolutionRow candidate) {
        return criterionWeights.entrySet()
                .stream()
                .mapToDouble(criterion -> criterion.getKey().getRating(candidate) * criterion.getValue())
                .sum();
    }

    /**
     * @return A list of rows of planks which can be placed on the base plank and a list of the remaining planks that do
     * not fit onto the base plank (besides the already added ones).
     */
    private Pair<List<PlankSolutionRow>, Set<RequiredPlank>> determineSolution(
            BasePlank basePlank, Set<RequiredPlank> requiredPlanks) {
        List<PlankSolutionRow> placedPlanks = new ArrayList<>();
        Set<RequiredPlank> ignoredPlanks;
        if (basePlank == null) {
            ignoredPlanks = requiredPlanks;
        } else {
            Collection<RequiredPlank> rotatedPlanks = requiredPlanks.stream()
                    .map(plank -> plank.matchesGrainDirection(basePlank.getGrainDirection()) ? plank : plank.rotated())
                    .collect(Collectors.toList());
            List<RequiredPlank> planksToPlaceByHeight = rotatedPlanks.stream()
                    .map(plank -> plank.matchesGrainDirection(basePlank.getGrainDirection()) ? plank : plank.rotated())
                    .sorted((p1, p2) -> p2.getHeight() - p1.getHeight())
                    .collect(Collectors.toList());
            List<RequiredPlank> planksToPlaceByWidth = rotatedPlanks.stream()
                    .map(plank -> plank.matchesGrainDirection(basePlank.getGrainDirection()) ? plank : plank.rotated())
                    .sorted((p1, p2) -> p2.getWidth() - p1.getWidth())
                    .collect(Collectors.toList());

            Optional<BasePlank> remainingBasePlank = Optional.of(basePlank);
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
                    bestCandidate.getPlanks()
                            .forEach(p -> p.setPlacedInSolution(true));
                    planksToPlaceByHeight.removeAll(bestCandidate.getPlanks());
                    planksToPlaceByWidth.removeAll(bestCandidate.getPlanks());
                }
            }
            // NOTE Ignored planks may not have same rotation as initially given
            ignoredPlanks = new HashSet<>(planksToPlaceByWidth);
            ignoredPlanks.forEach(p -> p.setPlacedInSolution(false));
        }
        return new Pair<>(placedPlanks, ignoredPlanks);
    }

    @SuppressWarnings("unchecked")
    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        initializeTransientMember();

        input.defaultReadObject();
        criterionWeightsProperty()
                .putAll((HashMap<PlankSolutionCriterion, Double>) input.readObject());
        requiredPlanksProperty()
                .addAll((HashSet<RequiredPlank>) input.readObject());
        setBasePlank((BasePlank) input.readObject());
        proposedSolution.set(determineSolution(getBasePlank(), getRequiredPlanks()));
    }

    @Serial
    private void writeObject(ObjectOutputStream output) throws IOException {
        output.defaultWriteObject();
        output.writeObject(new HashMap<>(criterionWeightsProperty()));
        output.writeObject(new HashSet<>(getRequiredPlanks()));
        output.writeObject(getBasePlank());
    }

    public ObservableMap<PlankSolutionCriterion, Double> criterionWeightsProperty() {
        // FIXME How to avoid that null values are set as weight
        return criterionWeights;
    }

    public double getCriterionWeight(PlankSolutionCriterion criterion) {
        return criterionWeights.get(criterion);
    }

    public void setCriterionWeight(PlankSolutionCriterion criterion, double weight) {
        criterionWeights.put(criterion, weight);
    }

    public SetProperty<RequiredPlank> requiredPlanksProperty() {
        return requiredPlanks;
    }

    public ObservableSet<RequiredPlank> getRequiredPlanks() {
        return requiredPlanksProperty().get();
    }

    public void setRequiredPlanks(ObservableSet<RequiredPlank> requiredPlanks) {
        requiredPlanksProperty().set(requiredPlanks);
    }

    public ObjectProperty<BasePlank> basePlankProperty() {
        return basePlank;
    }

    public BasePlank getBasePlank() {
        return basePlankProperty().get();
    }

    public void setBasePlank(BasePlank basePlank) {
        basePlankProperty().set(basePlank);
    }

    public ReadOnlyObjectProperty<Pair<List<PlankSolutionRow>, Set<RequiredPlank>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Pair<List<PlankSolutionRow>, Set<RequiredPlank>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }
}
