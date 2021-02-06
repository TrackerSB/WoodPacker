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
    private static final long serialVersionUID = 1L;
    // FIXME Should there be a subclass of RequiredPlank like PlacedPlank containing the following additional property?
    // FIXME Solely PlankProblem::determineSolution(...) should be allowed to change this member
    private transient /*final*/ BooleanProperty placedInSolution = new SimpleBooleanProperty(false);

    public RequiredPlank(final String plankId, final int width, final int height,
                         final PlankGrainDirection grainDirection) {
        super(plankId, width, height, grainDirection);
    }

    public RequiredPlank(final String plankId, final int width, final int height,
                         final PlankGrainDirection grainDirection, final String comment) {
        super(plankId, width, height, grainDirection, comment);
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

    @Serial
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        placedInSolution = new SimpleBooleanProperty(input.readBoolean());
    }

    @Serial
    private void writeObject(final ObjectOutputStream output) throws IOException, ClassNotFoundException {
        output.defaultWriteObject();
        output.writeBoolean(isPlacedInSolution());
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
