package co.ata.pathcraft.features;

import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import net.minecraft.text.Text;

public class LevelFeat extends BonusFeat {
    public static FeatureInstance Instance(int level) {
        FeatureInstance instance = new FeatureInstance(Feature.LEVEL_FEAT);
        instance.data.putInt("level", level);
        return instance;
    }

    public int getFeatLevel(FeatureInstance instance) {
        if (instance.data.contains("level"))
            return instance.data.getInt("level");
        return 0;
    }

    public void setFeatLevel(FeatureInstance instance, int level) {
        instance.data.putInt("level", level);
    }

    @Override
    public Text getName(FeatureInstance instance) {
        return Text.translatable("feature.pathcraft:level_feat.name", getFeatLevel(instance));
    }
}
