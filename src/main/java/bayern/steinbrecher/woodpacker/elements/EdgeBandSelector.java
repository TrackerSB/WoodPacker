package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.CheckedControl;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.woodpacker.data.EdgeBand;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Set;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class EdgeBandSelector extends Control implements CheckedControl {
    private final CheckedControl ccBase = new CheckableControlBase<>(this);
    private final SetProperty<EdgeBand> selected = new SimpleSetProperty<>(FXCollections.observableSet());
    private final IntegerProperty thickness = new SimpleIntegerProperty(1);

    public EdgeBandSelector() {
        super();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EdgeBandSelectorSkin(this);
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return ccBase.validProperty();
    }

    @Override
    public boolean addValidityConstraint(ObservableBooleanValue constraint) {
        return ccBase.addValidityConstraint(constraint);
    }

    @Override
    public ObservableList<ReportEntry> getReports() {
        return ccBase.getReports();
    }

    @Override
    public boolean addReport(ReportEntry report) {
        return ccBase.addReport(report);
    }

    @Override
    public ReadOnlyBooleanProperty checkedProperty() {
        return ccBase.checkedProperty();
    }

    public SetProperty<EdgeBand> selectedProperty() {
        return selected;
    }

    public Set<EdgeBand> getSelected() {
        return selectedProperty().get();
    }

    public void setSelected(Set<EdgeBand> selected) {
        selectedProperty().clear();
        selectedProperty().addAll(selected);
    }

    public IntegerProperty thicknessProperty() {
        return thickness;
    }

    public int getThickness() {
        return thicknessProperty().get();
    }

    public void setThickness(int thickness) {
        thicknessProperty().set(thickness);
    }
}
