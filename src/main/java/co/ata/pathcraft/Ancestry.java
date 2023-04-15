package co.ata.pathcraft;

import java.util.HashMap;
import java.util.HashSet;

import co.ata.pathcraft.ancestries.*;
import net.minecraft.text.Text;

public abstract class Ancestry {
    public static final Ancestry HUMAN = new Human();
    public static final Ancestry ELF = new Elf();

    public void onInitialize() {
    };
    
    public abstract HashSet<FeatureInstance> getFeatures();

    public HashMap<Stat, Integer> RacialStats = new HashMap<Stat, Integer>();

    public Text getName() {
        return Text.translatable("ancestry." + PathCraftRegistries.ANCESTRY.getId(this) + ".name");
    }

    public Text getDescription() {
        return Text
                .translatable("ancestry." + PathCraftRegistries.ANCESTRY.getId(this) + ".description");
    }
    
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
