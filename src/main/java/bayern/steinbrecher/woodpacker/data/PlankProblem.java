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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankProblem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Sort {@link PlankSolutionRow} by descending area and ascending by the name of the pivot element ascending.
     */
    private static final Comparator<PlankVariationGroup> plankVariationGroupComparator = (rpA, rpB) -> {
        final RequiredPlank pivotA = rpA.getPivot();
        final RequiredPlank pivotB = rpB.getPivot();
        final int areaDifference = pivotB.getArea() - pivotA.getArea();
        if (areaDifference == 0) {
            return pivotA.getId().compareTo(pivotB.getId());
        }
        return areaDifference;
    };
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

    private double determineCandidateQuality(final PlankSolutionRow candidate) {
        return criterionWeights.entrySet()
                .stream()
                .mapToDouble(criterion -> criterion.getKey().getRating(candidate, this) * criterion.getValue())
                .sum();
    }

    private PlankSolutionRow createCandidate(
            final boolean horizontal, final Point2D basePlankOffset, final Plank availablePlank,
            final Set<PlankVariationGroup> plankVariations) {
        assert !plankVariations.isEmpty() : "No planks left for creating a candidate";
        int maxLength;
        int maxBreadth;
        if (horizontal) {
            maxLength = availablePlank.getWidth();
            maxBreadth = availablePlank.getHeight();
        } else {
            maxLength = availablePlank.getHeight();
            maxBreadth = availablePlank.getWidth();
        }
        final PlankSolutionRow finalCandidate
                = new PlankSolutionRow(basePlankOffset, horizontal, maxLength, maxBreadth);
        double finalQuality = determineCandidateQuality(finalCandidate);
        for (PlankVariationGroup group : plankVariations) {
            final Optional<Pair<RequiredPlank, Double>> optBestVariant = group.getVariations()
                    .stream()
                    .map(v -> {
                        final PlankSolutionRow currentVariant = new PlankSolutionRow(finalCandidate);
                        if (currentVariant.addPlank(v)) {
                            final double currentVariantQuality = determineCandidateQuality(currentVariant);
                            return new Pair<>(v, currentVariantQuality);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .max(Comparator.comparing(Pair::getValue));
            if (optBestVariant.isPresent()) {
                final Pair<RequiredPlank, Double> bestVariant = optBestVariant.get();
                if (bestVariant.getValue() > finalQuality) {
                    finalCandidate.addPlank(bestVariant.getKey());
                    finalQuality = bestVariant.getValue();
                }
            }
        }
        return finalCandidate;
    }

    /**
     * @return A list of rows of planks which can be placed on the base plank and a list of the remaining planks that do
     * not fit onto the base plank (besides the already added ones).
     */
    private Pair<List<PlankSolutionRow>, Set<RequiredPlank>> determineSolution(
            final BasePlank basePlank, final Set<RequiredPlank> requiredPlanks) {
        final List<PlankSolutionRow> placedPlanks = new ArrayList<>();
        Set<RequiredPlank> ignoredPlanks;
        if (basePlank == null) {
            ignoredPlanks = requiredPlanks;
        } else {
            /* The following collection contains all not yet placed planks in all variations in which they are allowed.
             * In case the base plank as well as the required plank have a grain direction there is only one allowed
             * variation of the plank. If either the base plank or the required plank have no grain direction the
             * collection contains two versions of the plank (i.e. rotated and not rotated).
             */
            final SortedSet<PlankVariationGroup> unplacedPlank = requiredPlanks.stream()
                    .peek(rp -> rp.setPlacedInSolution(false))
                    .map(rp -> new PlankVariationGroup(rp, basePlank))
                    // Sort by area decreasing
                    .collect(Collectors.toCollection(() -> new TreeSet<>(plankVariationGroupComparator)));

            Map<Point2D, BasePlank> remainingPartitions = new HashMap<>();
            remainingPartitions.put(Point2D.ZERO, basePlank);
            while (!remainingPartitions.isEmpty() && !unplacedPlank.isEmpty()) {
                // Determine best candidate
                final Optional<Pair<PlankSolutionRow, Double>> optBestCandidate = remainingPartitions.entrySet()
                        .stream()
                        .flatMap(entry -> Stream.of(
                                createCandidate(true, entry.getKey(), entry.getValue(), unplacedPlank),
                                createCandidate(false, entry.getKey(), entry.getValue(), unplacedPlank)))
                        .filter(c -> c.getPlanks().size() > 0)
                        .map(c -> new Pair<>(c, determineCandidateQuality(c)))
                        .max(Comparator.comparing(Pair::getValue));
                if (optBestCandidate.isPresent()) {
                    final Pair<PlankSolutionRow, Double> bestCandidate = optBestCandidate.get();
                    final PlankSolutionRow bestCandidateRow = bestCandidate.getKey();
                    final Point2D selectedOffset = bestCandidateRow.getStartOffset();
                    final BasePlank selectedPartition = remainingPartitions.remove(selectedOffset);

                    // Split partition containing the best candidate into remaining partitions
                    if (bestCandidateRow.addHorizontal()) {
                        final Optional<BasePlank> remainingPartitionNotInRow
                                = selectedPartition.heightDecreased(bestCandidateRow.getCurrentBreadth());
                        remainingPartitionNotInRow.ifPresent(
                                bp -> remainingPartitions.put(
                                        selectedOffset.add(0, bestCandidateRow.getCurrentBreadth()), bp));
                        final Optional<BasePlank> remainingPartitionInRow = selectedPartition.heightDecreased(
                                remainingPartitionNotInRow.map(Plank::getHeight).orElse(0))
                                .flatMap(bp -> bp.widthDecreased(bestCandidateRow.getCurrentLength()));
                        remainingPartitionInRow.ifPresent(
                                bp -> remainingPartitions.put(
                                        selectedOffset.add(bestCandidateRow.getCurrentLength(), 0), bp));
                    } else {
                        final Optional<BasePlank> remainingPartitionNotInRow
                                = selectedPartition.widthDecreased(bestCandidateRow.getCurrentBreadth());
                        remainingPartitionNotInRow.ifPresent(
                                bp -> remainingPartitions.put(
                                        selectedOffset.add(bestCandidateRow.getCurrentBreadth(), 0), bp));
                        final Optional<BasePlank> remainingPartitionInRow = selectedPartition.widthDecreased(
                                remainingPartitionNotInRow.map(Plank::getWidth).orElse(0))
                                .flatMap(bp -> bp.heightDecreased(bestCandidateRow.getCurrentLength()));
                        remainingPartitionInRow.ifPresent(
                                bp -> remainingPartitions.put(
                                        selectedOffset.add(0, bestCandidateRow.getCurrentLength()), bp));
                    }

                    placedPlanks.add(bestCandidateRow);
                    bestCandidateRow.getPlanks()
                            .forEach(p -> unplacedPlank.removeIf(up -> {
                                final RequiredPlank pivot = up.getPivot();
                                final boolean wasPlaced = pivot.equals(p);
                                if (wasPlaced) {
                                    pivot.setPlacedInSolution(true);
                                }
                                return wasPlaced;
                            }));
                } else {
                    /* NOTE 2021-02-06: This is the case if the best candidate contains no planks. This may be the case
                     * if all criteria have a weight of zero.
                     */
                    break;
                }
            }

            ignoredPlanks = unplacedPlank.stream()
                    .map(PlankVariationGroup::getPivot)
                    .collect(Collectors.toSet());
            ignoredPlanks.forEach(p -> p.setPlacedInSolution(false));
        }
        return new Pair<>(placedPlanks, ignoredPlanks);
    }

    @SuppressWarnings("unchecked")
    @Serial
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
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
    private void writeObject(final ObjectOutputStream output) throws IOException {
        output.defaultWriteObject();
        output.writeObject(new HashMap<>(criterionWeightsProperty()));
        output.writeObject(new HashSet<>(getRequiredPlanks()));
        output.writeObject(getBasePlank());
    }

    public ObservableMap<PlankSolutionCriterion, Double> criterionWeightsProperty() {
        // FIXME How to avoid that null values are set as weight
        return criterionWeights;
    }

    public double getCriterionWeight(final PlankSolutionCriterion criterion) {
        return criterionWeights.get(criterion);
    }

    public void setCriterionWeight(final PlankSolutionCriterion criterion, final double weight) {
        criterionWeights.put(criterion, weight);
    }

    public SetProperty<RequiredPlank> requiredPlanksProperty() {
        return requiredPlanks;
    }

    public ObservableSet<RequiredPlank> getRequiredPlanks() {
        return requiredPlanksProperty().get();
    }

    public void setRequiredPlanks(final ObservableSet<RequiredPlank> requiredPlanks) {
        requiredPlanksProperty().set(requiredPlanks);
    }

    public ObjectProperty<BasePlank> basePlankProperty() {
        return basePlank;
    }

    public BasePlank getBasePlank() {
        return basePlankProperty().get();
    }

    public void setBasePlank(final BasePlank basePlank) {
        basePlankProperty().set(basePlank);
    }

    public ReadOnlyObjectProperty<Pair<List<PlankSolutionRow>, Set<RequiredPlank>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Pair<List<PlankSolutionRow>, Set<RequiredPlank>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }

    private static class PlankVariationGroup {
        private final RequiredPlank pivot;
        private final List<RequiredPlank> variations = new ArrayList<>();

        public PlankVariationGroup(RequiredPlank pivot, BasePlank basePlank) {
            this.pivot = pivot;

            /* A plank can be placed if either its grain direction the base planks grain direction is irrelevant or
             * the grain direction matches the base planks grain direction.
             */
            if (pivot.matchesGrainDirection(basePlank.getGrainDirection())) {
                variations.add(pivot);
            }
            RequiredPlank rotatedPivot = pivot.rotated();
            if (rotatedPivot.matchesGrainDirection(basePlank.getGrainDirection())) {
                variations.add(pivot.rotated());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PlankVariationGroup that = (PlankVariationGroup) o;
            return this.getPivot().equals(that.getPivot());
        }

        @Override
        public int hashCode() {
            return getPivot().hashCode();
        }

        /**
         * @return The pivot element which yielded all variations. NOTE It is not necessarily the case that this pivot
         * is an allowed variation itself.
         */
        public RequiredPlank getPivot() {
            return pivot;
        }

        public List<RequiredPlank> getVariations() {
            return Collections.unmodifiableList(variations);
        }
    }
}
