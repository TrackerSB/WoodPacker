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
    // FIXME Solely PlankProblem::determineSolution(...) should be allowed to change this member
    private transient /*final*/ BooleanProperty placedInSolution = new SimpleBooleanProperty(false);

    public RequiredPlank(String id, int width, int height, PlankGrainDirection grainDirection) {
        super(id, width, height, grainDirection);
    }

    public RequiredPlank(String id, int width, int height, PlankGrainDirection grainDirection, String comment) {
        super(id, width, height, grainDirection, comment);
    }

    public RequiredPlank rotated() {
        PlankGrainDirection rotatedGrainDirection = switch (getGrainDirection()) {
            case HORIZONTAL -> PlankGrainDirection.VERTICAL;
            case VERTICAL -> PlankGrainDirection.HORIZONTAL;
            case IRRELEVANT -> PlankGrainDirection.IRRELEVANT;
        };
        return new RequiredPlank(getId(), getHeight(), getWidth(), rotatedGrainDirection, getComment());
    }

    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        placedInSolution = new SimpleBooleanProperty(input.readBoolean());
    }

    @Serial
    private void writeObject(ObjectOutputStream output) throws IOException, ClassNotFoundException {
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

    public void setPlacedInSolution(boolean placedInSolution) {
        placedInSolutionProperty().set(placedInSolution);
    }
}
