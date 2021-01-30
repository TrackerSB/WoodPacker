package bayern.steinbrecher.woodpacker.screens;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class ExportFailedException extends Exception {
    public ExportFailedException() {
    }

    public ExportFailedException(String message) {
        super(message);
    }

    public ExportFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExportFailedException(Throwable cause) {
        super(cause);
    }
}
