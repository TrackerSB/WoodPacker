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

    public RequiredPlank(final String id, final int width, final int height, final PlankGrainDirection grainDirection) {
        super(id, width, height, grainDirection);
    }

    public RequiredPlank(final String id, final int width, final int height, final PlankGrainDirection grainDirection,
                         final String comment) {
        super(id, width, height, grainDirection, comment);
    }

    public RequiredPlank rotated() {
        final PlankGrainDirection rotatedGrainDirection = switch (getGrainDirection()) {
            case HORIZONTAL -> PlankGrainDirection.VERTICAL;
            case VERTICAL -> PlankGrainDirection.HORIZONTAL;
            case IRRELEVANT -> PlankGrainDirection.IRRELEVANT;
        };
        return new RequiredPlank(getId(), getHeight(), getWidth(), rotatedGrainDirection, getComment());
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
        if (getComment() == null || getComment().isBlank()) {
            return String.format("\"%s\": %d [mm] x %d [mm]", getId(), getWidth(), getHeight());
        } else {
            return String.format("\"%s\": %d [mm] x %d [mm]\n%s",
                    getId(), getWidth(), getHeight(), getComment());
        }
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
