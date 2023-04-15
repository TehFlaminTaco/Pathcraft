package co.ata.pathcraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public abstract class Ability {
    public enum Frequency {
        AtWill,
        PerDay
    }

    public Frequency frequency = Frequency.AtWill;
    public int maxUses = -1;
    public int cooldownTicks = 20 * 3;

    public abstract boolean CanUse(AbilityInstance instance, PlayerEntity user);

    public abstract boolean Use(AbilityInstance instance, PlayerEntity user);
    
    public Text getName(AbilityInstance instance) {
        return this.getName();
    }

    public Text getDescription(AbilityInstance instance) {
        return this.getDescription();
    }

    public Text getName() {
        return Text.translatable("ability." + PathCraftRegistries.ABILITY.getId(this).toString() + ".name");
    }

    public Text getDescription() {
        return Text.translatable("ability." + PathCraftRegistries.ABILITY.getId(this).toString() + ".description");
    }

}
