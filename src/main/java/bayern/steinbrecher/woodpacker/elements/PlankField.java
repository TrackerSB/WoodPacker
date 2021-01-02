package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.Reportable;
import bayern.steinbrecher.checkedElements.report.ReportableBase;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankField extends Control implements Reportable {
    private final IntegerProperty plankWidth = new SimpleIntegerProperty();
    private final IntegerProperty plankHeight = new SimpleIntegerProperty();
    private final ObjectProperty<PlankGrainDirection> grainDirection = new SimpleObjectProperty<>();
    private final BooleanProperty skinElementsValid = new SimpleBooleanProperty(true);
    private final ReportableBase<PlankField> rBase = new ReportableBase<>(this);

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlankFieldSkin(this);
    }

    public Plank createPlank(String id) {
        return new Plank(id, getPlankWidth(), getPlankHeight(), getGrainDirection());
    }

    // FIXME Only skins for PlankFields should be allowed to call this method
    void bindSkinElementsValidProperty(ObservableValue<Boolean> validity) {
        skinElementsValid.bind(validity);
    }

    public IntegerProperty plankWidthProperty() {
        return plankWidth;
    }

    public int getPlankWidth() {
        return plankWidthProperty().get();
    }

    public void setPlankWidth(int plankWidth) {
        plankWidthProperty().set(plankWidth);
    }

    public IntegerProperty plankHeightProperty() {
        return plankHeight;
    }

    public int getPlankHeight() {
        return plankHeightProperty().get();
    }

    public void setPlankHeight(int plankHeight) {
        plankHeightProperty().set(plankHeight);
    }

    public ObjectProperty<PlankGrainDirection> grainDirectionProperty() {
        return grainDirection;
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirectionProperty().get();
    }

    public void setGrainDirection(PlankGrainDirection direction) {
        grainDirectionProperty().set(direction);
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return rBase.validProperty();
    }

    @Override
    public boolean isValid() {
        return rBase.isValid();
    }

    @Override
    public boolean addValidityConstraint(ObservableBooleanValue constraint) {
        return rBase.addValidityConstraint(constraint);
    }

    @Override
    public ObservableList<ReportEntry> getReports() {
        return rBase.getReports();
    }

    @Override
    public boolean addReport(ReportEntry report) {
        return rBase.addReport(report);
    }
}
