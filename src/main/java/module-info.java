module bayern.steinbrecher.WoodPacker {
    requires bayern.steinbrecher.CheckedElements;
    requires bayern.steinbrecher.ScreenSwitcher;
    requires bayern.steinbrecher.Utility;
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.jetbrains.annotations;

    opens bayern.steinbrecher.woodpacker to javafx.graphics;
    opens bayern.steinbrecher.woodpacker.elements to javafx.fxml;
    opens bayern.steinbrecher.woodpacker.screens to javafx.fxml;
}