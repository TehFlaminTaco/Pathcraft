package co.ata.pathcraft.features;

import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;

public class PointBuy extends Feature {
    public boolean IsSpent(FeatureInstance instance) {
        return instance.data.contains("spent") && instance.data.getBoolean("spent");
    }

    public void Spend(FeatureInstance instance) {
        instance.data.putBoolean("spent", true);
    }
}
