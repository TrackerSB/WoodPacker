package bayern.steinbrecher.woodpacker.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class SerializationUtility {
    private SerializationUtility() {
        throw new UnsupportedOperationException("Construction of instances prohibited");
    }

    public static <T extends Serializable> byte[] serialize(final T toSerialize) throws IOException {
        final ByteArrayOutputStream serializedBasePlank = new ByteArrayOutputStream();
        try (final ObjectOutputStream serializer = new ObjectOutputStream(serializedBasePlank)) {
            serializer.writeObject(toSerialize);
            return serializedBasePlank.toByteArray();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(final byte[] toDeserialize)
            throws IOException, ClassNotFoundException {
        try (final ObjectInputStream deserializer = new ObjectInputStream(new ByteArrayInputStream(toDeserialize))) {
            Object deserializedObject = deserializer.readObject();
            return (T) deserializedObject;
        }
    }
}
