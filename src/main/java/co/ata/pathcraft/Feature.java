package co.ata.pathcraft;

import java.util.ArrayList;
import java.util.List;

import co.ata.pathcraft.features.*;
import co.ata.pathcraft.features.ancestry.elf.*;
import co.ata.pathcraft.features.classes.fighter.*;
import co.ata.pathcraft.features.classes.rogue.SneakAttack;
import co.ata.pathcraft.features.classes.wizard.Cantrips;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public abstract class Feature {

    public boolean initialized = false;

    // System Feats
    public static final BonusFeat BONUS_FEAT = new BonusFeat();
    public static final LevelFeat LEVEL_FEAT = new LevelFeat();
    public static final CombatBonusFeat COMBAT_BONUS_FEAT = new CombatBonusFeat();
    public static final PointBuy POINT_BUY = new PointBuy();

    // General Feats
    public static final Cleave CLEAVE = new Cleave();
    public static final WeaponFocus WEAPON_FOCUS = new WeaponFocus();
    public static final ArmorProficiency ARMOR_PROFICIENCY = new ArmorProficiency();
    public static final TwoWeaponFighting TWO_WEAPON_FIGHTING = new TwoWeaponFighting();

    // Ancestry Feats
    public static final ElvenMagic ELVEN_MAGIC = new ElvenMagic();
    public static final ElvenQuickness ELVEN_QUICKNESS = new ElvenQuickness();

    // Class Feats
    public static final ArmorTraining ARMOR_TRAINING = new ArmorTraining();
    public static final WeaponTraining WEAPON_TRAINING = new WeaponTraining();
    public static final SneakAttack SNEAK_ATTACK = new SneakAttack();
    public static final Cantrips CANTRIPS = new Cantrips();
    
    public FeatureType type = FeatureType.GENERAL;
    public void onInitialize() {};
    
    public void onAdd(PlayerEntity player, FeatureInstance instance) {};
    public void onRemove(PlayerEntity player, FeatureInstance instance) {};

    public void onLevelUp(PlayerEntity player, FeatureInstance instance) {
    };
    
    public boolean meetsPrerequisites(PlayerEntity player, FeatureInstance instance) {
        return true;
    };

    public boolean meetsPrerequisites(FeatureInstance instance) {
        return meetsPrerequisites(PathCraftClient.MC.player, instance);
    };
    
    public Text getName(FeatureInstance instance) {
        return Text.translatable("feature." + PathCraftRegistries.FEATURE.getId(this) + ".name");
    }

    public Text getDescription(FeatureInstance instance) {
        return Text.translatable(
                "feature." + PathCraftRegistries.FEATURE.getId(this) + ".description");
    }
    
    public Text getPrerequisites(FeatureInstance instance) {
        return Text.translatable(
                "feature." + PathCraftRegistries.FEATURE.getId(this) + ".prerequisites");
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public List<FeatureInstance> Selectable(String category) {
        return new ArrayList<FeatureInstance>();
    }

    public boolean InstanceEquals(FeatureInstance a, FeatureInstance b) {
        return a.feature == b.feature;
    }
}
