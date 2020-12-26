module bayern.steinbrecher.WoodPacker {
    requires bayern.steinbrecher.ScreenSwitcher;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.jetbrains.annotations;

    opens bayern.steinbrecher.woodPacker to javafx.graphics;
    opens bayern.steinbrecher.woodPacker.screens to javafx.fxml;
}