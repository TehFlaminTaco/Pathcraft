package co.ata.pathcraft.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface SlowdownForArmor {
    Event<SlowdownForArmor> EVENT = EventFactory.createArrayBacked(SlowdownForArmor.class,
            (listeners) -> (player, armor, slot, current) -> {
                int slowDown = current;
                for (SlowdownForArmor listener : listeners) {
                    slowDown = listener.getSlowdownForArmor(player, armor, slot, slowDown);
                }
                return slowDown;
        }
    );

    public int getSlowdownForArmor(PlayerEntity player, ItemStack armor, EquipmentSlot slot, int current);
}
