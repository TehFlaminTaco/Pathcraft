package co.ata.pathcraft;

import java.util.List;

import co.ata.pathcraft.data.PlayerPathData;
import co.ata.pathcraft.features.ArmorProficiency;
import net.minecraft.entity.player.PlayerEntity;

public class Prerequisites {
    private boolean _isMet = true;
    public static Prerequisites begin() {
        return new Prerequisites();
    }

    public boolean get() {
        return _isMet;
    }

    public Prerequisites and(boolean value) {
        _isMet = _isMet && value;
        return this;
    }

    public Prerequisites or(boolean value) {
        _isMet = _isMet || value;
        return this;
    }

    public Prerequisites not() {
        _isMet = !_isMet;
        return this;
    }

    public Prerequisites is(boolean value) {
        _isMet = _isMet == value;
        return this;
    }

    public Prerequisites HasAncestry(PlayerEntity player, Ancestry ancestry) {
        if (!_isMet)
            return this;
        if (player.world.isClient) {
            _isMet = PathCraftClient.data.ancestry == ancestry;
            return this;
        }
        _isMet = PlayerPathData.get(player).getAncestry() == ancestry;
        return this;
    }

    public Prerequisites HasFeature(PlayerEntity player, Feature feature) {
        if (!_isMet)
            return this;
        if (player.world.isClient) {
            _isMet = PathCraftClient.data.features.stream().anyMatch((f) -> f.feature == feature);
            return this;
        }
        _isMet = PlayerPathData.get(player).hasFeature(feature);
        return this;
    }

    public Prerequisites HasStat(PlayerEntity player, Stat stat, int min) {
        if (!_isMet)
            return this;
        if (player.world.isClient) {
            _isMet = PathCraftClient.data.stats.get(stat) >= min;
            return this;
        }
        _isMet = PlayerPathData.get(player).stats.get(stat) >= min;
        return this;
    }

    public Prerequisites HasArmorProficiency(PlayerEntity player, String category) {
        if(!_isMet)
            return this;
        List<FeatureInstance> features;
        if(player.world.isClient){
            features = PathCraftClient.data.features;
        } else {
            features = PlayerPathData.get(player).getFeatures();
        }
        _isMet = features.stream().anyMatch((f) -> f.feature instanceof ArmorProficiency
                && ((ArmorProficiency) f.feature).GetArmorClass(f).equals(category));
        return this;
    }
}
