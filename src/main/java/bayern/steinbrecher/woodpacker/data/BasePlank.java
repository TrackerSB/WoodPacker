package bayern.steinbrecher.woodpacker.data;

import bayern.steinbrecher.woodpacker.WoodPacker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlank extends Plank {
    @Serial
    private static final long serialVersionUID = 871234122134L;
    private static final long INTERNAL_SERIAL_VERSION = 1L;

    // Since internal serial version 1
    private /*final*/ PlankMaterial material;

    public BasePlank(final String plankId, final int width, final int height, final PlankGrainDirection grainDirection,
                     final PlankMaterial material) {
        super(plankId, width, height, grainDirection);
        this.material = material;
    }

    public BasePlank(final String plankId, final int width, final int height, final PlankGrainDirection grainDirection,
                     final PlankMaterial material, final String comment) {
        super(plankId, width, height, grainDirection, comment);
        this.material = material;
    }

    public Optional<BasePlank> heightDecreased(final int decreaseBy) {
        Optional<BasePlank> resizedPlank;
        if (decreaseBy >= getHeight()) {
            resizedPlank = Optional.empty();
        } else {
            resizedPlank = Optional.of(
                    new BasePlank(getPlankId(), getWidth(), getHeight() - decreaseBy, getGrainDirection(),
                            getMaterial(), getComment()));
        }
        return resizedPlank;
    }

    public Optional<BasePlank> widthDecreased(final int decreaseBy) {
        Optional<BasePlank> resizedPlank;
        if (decreaseBy >= getWidth()) {
            resizedPlank = Optional.empty();
        } else {
            resizedPlank = Optional.of(
                    new BasePlank(getPlankId(), getWidth() - decreaseBy, getHeight(), getGrainDirection(),
                            getMaterial(), getComment()));
        }
        return resizedPlank;
    }

    @Override
    public String toString() {
        final String localizedMaterial = WoodPacker.getResource(getMaterial().getResourceKey());
        String plankDescription;
        if (getComment() == null || getComment().isBlank()) {
            plankDescription = String.format("\"%s\": %d [mm] x %d [mm] (%s)", getPlankId(), getWidth(), getHeight(),
                    localizedMaterial);
        } else {
            plankDescription = String.format("\"%s\": %d [mm] x %d [mm] (%s)\n%s",
                    getPlankId(), getWidth(), getHeight(), localizedMaterial, getComment());
        }
        return plankDescription;
    }

    @Serial
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        final long inputSerialVersion = input.readLong();

        // Internal serial version 1
        assert inputSerialVersion >= 1 : "The internal serial version must be at least 1";
        material = (PlankMaterial) input.readObject();
    }

    @Serial
    private void writeObject(final ObjectOutputStream output) throws IOException {
        output.writeLong(INTERNAL_SERIAL_VERSION);

        // Internal serial version 1
        output.writeObject(getMaterial());
    }

    public PlankMaterial getMaterial() {
        return material;
    }
}
