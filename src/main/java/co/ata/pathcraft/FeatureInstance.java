package co.ata.pathcraft;

import java.util.ArrayList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FeatureInstance {
    public ArrayList<AbilityInstance> providedAbilities = new ArrayList<AbilityInstance>();

    public FeatureInstance(Feature feature) {
        this.feature = feature;
        this.data = new NbtCompound();
    }

    public FeatureInstance(Feature feature, NbtCompound data) {
        this.feature = feature;
        this.data = data;
    }
    private FeatureInstance() {
    }

    public Feature feature;
    public NbtCompound data;

    public NbtCompound writeNbtCompound(NbtCompound nbt) {
        nbt.putString("feature", PathCraftRegistries.FEATURE.getId(feature).toString());
        nbt.put("data", data);
        NbtList abilities = new NbtList();
        for (AbilityInstance ability : providedAbilities) {
            abilities.add(ability.writeNbtCompound(new NbtCompound()));
        }
        nbt.put("abilities", abilities);
        return nbt;
    }

    public static FeatureInstance createFromNbt(NbtCompound nbt) {
        FeatureInstance instance = new FeatureInstance();
        instance.feature = PathCraftRegistries.FEATURE.get(new Identifier(nbt.getString("feature")));
        instance.data = nbt.getCompound("data");
        if(nbt.contains("abilities")) {
            NbtList abilities = nbt.getList("abilities", NbtList.COMPOUND_TYPE);
            for (int i = 0; i < abilities.size(); i++) {
                instance.providedAbilities.add(AbilityInstance.createFromNbt(abilities.getCompound(i)));
            }
        }
        return instance;
    }

    public Text getName() {
        return feature.getName(this);
    }

    public Text getDescription() {
        return feature.getDescription(this);
    }

    public boolean meetsPrerequisites(PlayerEntity player) {
        return feature.meetsPrerequisites(player, this);
    }

    public boolean meetsPrerequisites() {
        return feature.meetsPrerequisites(this);
    }


}
