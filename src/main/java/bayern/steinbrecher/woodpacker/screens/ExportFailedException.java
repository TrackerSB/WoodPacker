package bayern.steinbrecher.woodpacker.screens;

import java.io.Serial;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class ExportFailedException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ExportFailedException() {
        super();
    }

    public ExportFailedException(final String message) {
        super(message);
    }

    public ExportFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExportFailedException(final Throwable cause) {
        super(cause);
    }
}
