package bayern.steinbrecher.woodpacker.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class RequiredPlank extends Plank {
    @Serial
    private static final long serialVersionUID = 98072354127L;
    private static final long internalSerialVersion = 1L;

    // Since internal serial version 1
    // FIXME Should there be a subclass of RequiredPlank like PlacedPlank containing the following additional property?
    // FIXME Solely PlankProblem::determineSolution(...) should be allowed to change this member
    private transient /*final*/ BooleanProperty placedInSolution;

    public RequiredPlank(final String plankId, final int width, final int height,
                         final PlankGrainDirection grainDirection) {
        super(plankId, width, height, grainDirection);
        initializeTransientMember();
    }

    public RequiredPlank(final String plankId, final int width, final int height,
                         final PlankGrainDirection grainDirection, final String comment) {
        super(plankId, width, height, grainDirection, comment);
        initializeTransientMember();
    }

    private void initializeTransientMember() {
        placedInSolution = new SimpleBooleanProperty(false);
    }

    public RequiredPlank rotated() {
        final PlankGrainDirection rotatedGrainDirection = switch (getGrainDirection()) {
            case HORIZONTAL -> PlankGrainDirection.VERTICAL;
            case VERTICAL -> PlankGrainDirection.HORIZONTAL;
            case IRRELEVANT -> PlankGrainDirection.IRRELEVANT;
        };
        return new RequiredPlank(getPlankId(), getHeight(), getWidth(), rotatedGrainDirection, getComment());
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
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        initializeTransientMember();
        final long inputSerialVersion = input.readLong();

        // Internal serial version 1
        if (inputSerialVersion >= 1) {
            setPlacedInSolution(input.readBoolean());
        }
    }

    @Serial
    private void writeObject(final ObjectOutputStream output) throws IOException {
        output.writeLong(internalSerialVersion);

        // Internal serial version 1
        output.writeBoolean(isPlacedInSolution());
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
}
