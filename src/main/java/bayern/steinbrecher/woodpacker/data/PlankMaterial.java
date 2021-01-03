package bayern.steinbrecher.woodpacker.data;

import bayern.steinbrecher.woodpacker.WoodPacker;

public enum PlankMaterial {
    BEECH("beech"),
    BIRCH("birch"),
    BOG_OAK("bogOak"),
    FIR("fir"),
    OAK("oak"),
    PINE("pine"),
    SPRUCE("spruce"),
    UNDEFINED("undefined");

    private final String resourceKey;

    PlankMaterial(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    @Override
    public String toString() {
        return WoodPacker.LANGUAGE_BUNDLE.getString(getResourceKey());
    }
}
