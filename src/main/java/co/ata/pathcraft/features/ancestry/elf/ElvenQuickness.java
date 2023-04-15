package co.ata.pathcraft.features.ancestry.elf;

import java.util.UUID;

import co.ata.pathcraft.Feature;
import co.ata.pathcraft.Prerequisites;
import co.ata.pathcraft.data.PlayerPathData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class ElvenQuickness extends Feature {

    public static UUID elvenQuicknessAttribute = UUID.fromString("bbae7452-36a8-436b-9942-1210f04a6b09");

    public EntityAttributeModifier GetModifier() {
        return new EntityAttributeModifier(elvenQuicknessAttribute, "pathcraft:elvenquickness", 0.2d,
                EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }
    
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerPathData data = PlayerPathData.get(server, player);
                if (data == null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(elvenQuicknessAttribute);
                    continue;
                }
                boolean hasFeat = Prerequisites.begin().HasFeature(player, this).get();
                if (hasFeat) {
                    if (!player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            .hasModifier(GetModifier()))
                        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                                .addTemporaryModifier(this.GetModifier());
                }
                else {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            .removeModifier(elvenQuicknessAttribute);
                }
            }
        });
    }

}
