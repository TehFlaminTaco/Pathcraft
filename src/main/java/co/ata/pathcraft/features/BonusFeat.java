package co.ata.pathcraft.features;

import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;

public class BonusFeat extends Feature {
    public String Category = "general";
    public boolean IsSpent(FeatureInstance instance) {
        return instance.data.contains("spent") && instance.data.getBoolean("spent");
    }

    public void Spend(FeatureInstance instance) {
        instance.data.putBoolean("spent", true);
    }

    public boolean CanAcceptFeat(FeatureInstance instance, FeatureInstance feat) {
        if (IsSpent(instance))
            return false;
        return true;
    }
}
