package bayern.steinbrecher.woodpacker.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class RequiredPlank extends Plank {
    @Serial
    private static final long serialVersionUID = 98072354127L;
    private static final long INTERNAL_SERIAL_VERSION = 2L;

    // Since internal serial version 1
    // FIXME Should there be a subclass of RequiredPlank like PlacedPlank containing the following additional property?
    // FIXME Solely PlankProblem::determineSolution(...) should be allowed to change this member
    private transient /*final*/ BooleanProperty placedInSolution;

    // Since internal serial version 2
    private transient /*final*/ SetProperty<EdgeBand> edgeBands;

    public RequiredPlank(final String plankId, final int width, final int height,
                         final PlankGrainDirection grainDirection) {
        super(plankId, width, height, grainDirection);
        initializeTransientMember();
    }

    public RequiredPlank(final String plankId, final int width, final int height,
                         final PlankGrainDirection grainDirection, final String comment,
                         final Set<EdgeBand> edgeBands) {
        super(plankId, width, height, grainDirection, comment);
        initializeTransientMember();
        this.edgeBands.get().clear();
        this.edgeBands.get().addAll(edgeBands);
    }

    private void initializeTransientMember() {
        placedInSolution = new SimpleBooleanProperty(false);
        edgeBands = new SimpleSetProperty<>(FXCollections.observableSet());
    }

    public RequiredPlank rotated() {
        final PlankGrainDirection rotatedGrainDirection = switch (getGrainDirection()) {
            case HORIZONTAL -> PlankGrainDirection.VERTICAL;
            case VERTICAL -> PlankGrainDirection.HORIZONTAL;
            case IRRELEVANT -> PlankGrainDirection.IRRELEVANT;
        };
        final Set<EdgeBand> rotatedEdgeBands
                = edgeBands.stream()
                .map(eb ->
                        switch (eb) {
                            case LEFT -> EdgeBand.UPPER;
                            case UPPER -> EdgeBand.RIGHT;
                            case RIGHT -> EdgeBand.LOWER;
                            case LOWER -> EdgeBand.LEFT;
                        }
                )
                .collect(Collectors.toSet());
        return new RequiredPlank(getPlankId(), getHeight(), getWidth(), rotatedGrainDirection, getComment(),
                rotatedEdgeBands);
    }

    public int getArea() {
        return getHeight() * getWidth();
    }

    @Override
    public String toString() {
        String plankDescription;
        if (getComment() == null || getComment().isBlank()) {
            plankDescription = String.format("\"%s\": %d [mm] x %d [mm]", getPlankId(), getWidth(), getHeight());
        } else {
            plankDescription = String.format("\"%s\": %d [mm] x %d [mm]\n%s",
                    getPlankId(), getWidth(), getHeight(), getComment());
        }
        return plankDescription;
    }

    @Serial
    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "unchecked"})
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        initializeTransientMember();
        final long inputSerialVersion = input.readLong();

        // Internal serial version 1
        assert inputSerialVersion >= 1 : "The internal serial version must be at least 1";
        setPlacedInSolution(input.readBoolean());

        // Internal serial version 2
        if (inputSerialVersion >= 2) {
            edgeBandsProperty()
                    .addAll((HashSet<EdgeBand>) input.readObject());
        } else {
            edgeBandsProperty().clear();
        }
    }

    @Serial
    private void writeObject(final ObjectOutputStream output) throws IOException {
        output.writeLong(INTERNAL_SERIAL_VERSION);

        // Internal serial version 1
        output.writeBoolean(isPlacedInSolution());

        // Internal serial version 2
        output.writeObject(new HashSet<>(getEdgeBands()));
    }

    public BooleanProperty placedInSolutionProperty() {
        return placedInSolution;
    }

    public boolean isPlacedInSolution() {
        return placedInSolutionProperty().get();
    }

    public void setPlacedInSolution(final boolean placedInSolution) {
        placedInSolutionProperty().set(placedInSolution);
    }

    public SetProperty<EdgeBand> edgeBandsProperty() {
        return edgeBands;
    }

    public Set<EdgeBand> getEdgeBands() {
        return edgeBandsProperty().get();
    }

    public void setEdgeBands(final Set<EdgeBand> edgeBands) {
        edgeBandsProperty().clear();
        edgeBandsProperty().addAll(edgeBands);
    }
}
