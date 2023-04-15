package co.ata.pathcraft.ancestries;

import java.util.HashSet;

import co.ata.pathcraft.Ancestry;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;

public class Human extends Ancestry {
    @Override
    public HashSet<FeatureInstance> getFeatures() {
        HashSet<FeatureInstance> features = new HashSet<FeatureInstance>();
        features.add(new FeatureInstance(Feature.BONUS_FEAT));
        return features;
    }
}
