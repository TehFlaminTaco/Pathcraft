package co.ata.pathcraft;

import java.util.HashSet;

import co.ata.pathcraft.classes.*;
import net.minecraft.text.Text;

public abstract class Class {
    public static final Class FIGHTER = new Fighter();
    public static final Class ROGUE = new Rogue();
    public static final Class WIZARD = new Wizard();

    public Stat primaryStat = Stat.STRENGTH;

    public abstract HashSet<FeatureInstance> getLevelFeatures(int level);

    public Text getName() {
        return Text.translatable("class." + PathCraftRegistries.CLASS.getId(this) + ".name");
    }

    public Text getDescription() {
        return Text
                .translatable("class." + PathCraftRegistries.CLASS.getId(this) + ".description");
    }

    public Text cantripName() {
        return Text.translatable("class." + PathCraftRegistries.CLASS.getId(this) + ".cantrip");
    }
}
