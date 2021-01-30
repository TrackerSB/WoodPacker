package bayern.steinbrecher.woodpacker.screens;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class ExportFailedException extends Exception {
    public ExportFailedException() {
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
