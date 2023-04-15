package co.ata.pathcraft.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface MonsterKilled {
    Event<MonsterKilled> EVENT = EventFactory.createArrayBacked(MonsterKilled.class,
        (listeners) -> (player, monster) -> {
            for (MonsterKilled listener : listeners) {
                listener.onMonsterKilled(player, monster);
            }
        }
    );

    void onMonsterKilled(PlayerEntity player, LivingEntity monster);
}
