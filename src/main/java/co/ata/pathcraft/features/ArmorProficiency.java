package co.ata.pathcraft.features;

import java.util.ArrayList;
import java.util.List;

import co.ata.pathcraft.Category;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.FeatureType;
import co.ata.pathcraft.Prerequisites;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class ArmorProficiency extends Feature {
    public ArmorProficiency() {
        type = FeatureType.COMBAT;
    }

    public String GetArmorClass(FeatureInstance instance) {
        if (instance.data.contains("armorclass"))
            return instance.data.getString("armorclass");
        return "";
    }

    public static FeatureInstance Instance(String armorClass) {
        FeatureInstance instance = new FeatureInstance(Feature.ARMOR_PROFICIENCY);
        instance.data.putString("armorclass", armorClass);
        return instance;
    }

    @Override
    public Text getName(FeatureInstance instance) {
        return Text.translatable("feature.pathcraft:armor_proficiency.name", GetArmorClass(instance));
    }

    @Override
    public Text getDescription(FeatureInstance instance) {
        return Text.translatable("feature.pathcraft:armor_proficiency.description", GetArmorClass(instance));
    }

    @Override
    public Text getPrerequisites(FeatureInstance instance) {
        return Text.translatable("feature.pathcraft:armor_proficiency."+this.GetArmorClass(instance)+".prerequisites");
    }

    @Override
    public List<FeatureInstance> Selectable(String category) {
        List<FeatureInstance> list = new ArrayList<FeatureInstance>();
        if (Category.Is(category, "general,combat")) {
            list.add(Instance("light"));
            list.add(Instance("medium"));
            list.add(Instance("heavy"));
        }
        return list;
    }

    @Override
    public boolean meetsPrerequisites(PlayerEntity player, FeatureInstance instance) {
        switch (GetArmorClass(instance)) {
            case "light":
                return true;
            case "medium":
                return Prerequisites.begin().HasArmorProficiency(player, "light").get();
            case "heavy":
                return Prerequisites.begin().HasArmorProficiency(player, "medium").get();
        }
        return true;
    }

    @Override
    public boolean InstanceEquals(FeatureInstance a, FeatureInstance b) {
        return a.feature == b.feature && GetArmorClass(a).equals(GetArmorClass(b));
    }
}
