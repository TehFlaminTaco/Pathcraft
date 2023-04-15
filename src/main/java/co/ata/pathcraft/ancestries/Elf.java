package co.ata.pathcraft.ancestries;

import java.util.HashSet;

import co.ata.pathcraft.Ancestry;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.Stat;

public class Elf extends Ancestry {
    public Elf() {
        this.RacialStats.put(Stat.DEXTERITY, 2);
        this.RacialStats.put(Stat.INTELLIGENCE, 2);
        this.RacialStats.put(Stat.CONSTITUTION, -2);
    }

    @Override
    public HashSet<FeatureInstance> getFeatures() {
        HashSet<FeatureInstance> features = new HashSet<FeatureInstance>();
        features.add(new FeatureInstance(Feature.ELVEN_MAGIC));
        features.add(new FeatureInstance(Feature.ELVEN_QUICKNESS));
        return features;
    }
}
