package co.ata.pathcraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AbilityInstance {
    public Ability ability;
    public int uses = -1;
    public NbtCompound data = new NbtCompound();
    
    public AbilityInstance() {
    }

    public AbilityInstance(Ability ability) {
        this.ability = ability;
        this.uses = ability.maxUses;
    }

    public Text getName() {
        return ability.getName(this);
    }

    public Text getDescription() {
        return ability.getDescription(this);
    }

    public boolean CanUse(PlayerEntity user) {
        return ability.CanUse(this, user);
    }

    public boolean Use(PlayerEntity user) {
        return ability.Use(this, user);
    }

    public NbtCompound writeNbtCompound(NbtCompound nbt) {
        nbt.putString("ability", PathCraftRegistries.ABILITY.getId(ability).toString());
        nbt.put("data", data);
        nbt.putInt("uses", uses);
        return nbt;
    }

    public static AbilityInstance createFromNbt(NbtCompound nbt) {
        AbilityInstance instance = new AbilityInstance();
        instance.ability = PathCraftRegistries.ABILITY.get(new Identifier(nbt.getString("ability")));
        instance.data = nbt.getCompound("data");
        instance.uses = nbt.getInt("uses");
        return instance;
    }

}
