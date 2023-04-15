package co.ata.pathcraft.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

public interface ArmorBonus {
    Event<ArmorBonus> EVENT = EventFactory.createArrayBacked(ArmorBonus.class,
        (listeners) -> (player) -> {
            int bonus = 0;
            for (ArmorBonus listener : listeners) {
                bonus += listener.getArmorBonus(player);
            }
            return bonus;
        }
    );

    int getArmorBonus(PlayerEntity player);
}
