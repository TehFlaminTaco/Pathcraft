package co.ata.pathcraft.features;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Multimap;

import co.ata.pathcraft.Category;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.FeatureType;
import co.ata.pathcraft.Prerequisites;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.data.PlayerPathData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class TwoWeaponFighting extends Feature {
    public static UUID twoWeaponFightingAttackSpeed = UUID.fromString("fc7e5861-f39a-4533-941c-fba4e13f4fcb");

    public TwoWeaponFighting() {
        type = FeatureType.COMBAT;
    }

    public EntityAttributeModifier GetAttackSpeedModifier(int offhandSpeed) {
        return new EntityAttributeModifier(twoWeaponFightingAttackSpeed, "pathcraft:twoweaponfightingattackspeed",
                offhandSpeed,
                EntityAttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerPathData data = PlayerPathData.get(server, player);
                if (data == null || WeaponFocus.GetWeaponClass(player.getMainHandStack()) == "") {
                    player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                            .removeModifier(twoWeaponFightingAttackSpeed);
                    continue;
                }

                boolean hasFeat = data.hasFeature(this);
                int attackSpeed = 0;

                Multimap<EntityAttribute, EntityAttributeModifier> attrs = player.getOffHandStack()
                        .getAttributeModifiers(EquipmentSlot.MAINHAND);
                if (attrs.containsKey(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                    for (EntityAttributeModifier mod : attrs.get(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                        if (mod.getOperation() == EntityAttributeModifier.Operation.ADDITION) {
                            attackSpeed += (int) mod.getValue();
                            break;
                        }
                    }
                }
                if (hasFeat && attackSpeed > 0) {
                    if (player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                            .getModifier(twoWeaponFightingAttackSpeed) == null)
                        player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                                .addTemporaryModifier(this.GetAttackSpeedModifier(attackSpeed));
                } else {
                    player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED)
                            .removeModifier(twoWeaponFightingAttackSpeed);
                }
            }
        });
    }

    @Override
    public boolean meetsPrerequisites(PlayerEntity entity, FeatureInstance instance) {
        return Prerequisites.begin()
                .HasStat(entity, Stat.DEXTERITY, 13)
                .get();
    }
    
    @Override
    public List<FeatureInstance> Selectable(String category) {
        List<FeatureInstance> list = new ArrayList<FeatureInstance>();
        if (Category.Is(category, "general,combat"))
            list.add(new FeatureInstance(this));
        return list;
    }
}
