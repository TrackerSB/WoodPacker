package bayern.steinbrecher.woodpacker.internal;

public final class CompileSettings {
    private CompileSettings() {
        throw new UnsupportedOperationException("The construction of instances is prohibited");
    }

    public static boolean isGraphicalDebugEnabled() {
        return false;
    }
}
