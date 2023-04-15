package co.ata.pathcraft.events;


import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public interface TakeDamage {
    Event<TakeDamage> EVENT = EventFactory.createArrayBacked(TakeDamage.class,
            (listeners) -> (entity, source, amount) -> {
                for (TakeDamage listener : listeners) {
                    listener.onTakeDamage(entity, source, amount);
                }
        }
    );
    
    public void onTakeDamage(LivingEntity entity, DamageSource source, float amount);
}
