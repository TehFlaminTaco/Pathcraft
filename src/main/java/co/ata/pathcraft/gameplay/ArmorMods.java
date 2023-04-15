package co.ata.pathcraft.gameplay;

import java.util.UUID;

import co.ata.pathcraft.PathItemTags;
import co.ata.pathcraft.Prerequisites;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.data.PlayerPathData;
import co.ata.pathcraft.events.ArmorBonus;
import co.ata.pathcraft.events.SlowdownForArmor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class ArmorMods implements EndTick  {
    public static void init() {
        ArmorMods i = new ArmorMods();
        ServerTickEvents.END_SERVER_TICK.register(i);
    }

    public static final UUID armorSlowdownAttribute = UUID.fromString("83ab21a6-0e17-4144-98c5-ed75e09637ce");
    public EntityAttributeModifier GetSlowdownModifier(int amount) {
        return new EntityAttributeModifier(armorSlowdownAttribute, "pathcraft:armorslowdown", Math.pow(0.95d, amount)-1d, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }

    public static final UUID attributeArmorBonus = UUID.fromString("9692e0c7-8631-49d7-a9d2-81e271703065");
    public EntityAttributeModifier GetArmorBonusModifier(int amount) {
        return new EntityAttributeModifier(attributeArmorBonus, "pathcraft:armorbonus", amount * 2, EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerPathData data = PlayerPathData.get(player);
            if(data == null)continue;
            int slowdown = 0;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                int armorCat = getArmorCategory(player.getEquippedStack(slot), slot);
                int slotSlowdown = 0;
                switch (armorCat) {
                    case 0:
                        slotSlowdown = 0;
                        break;
                    case 1:
                        slotSlowdown = 2;
                        break;
                    case 2:
                        slotSlowdown = 4;
                        break;
                }
                int bestProf = -1;
                if (Prerequisites.begin().HasArmorProficiency(player, "heavy").get())
                    bestProf = 2;
                else if (Prerequisites.begin().HasArmorProficiency(player, "medium").get())
                    bestProf = 1;
                else if (Prerequisites.begin().HasArmorProficiency(player, "light").get())
                    bestProf = 0;
                
                if (armorCat > bestProf)
                    slotSlowdown += 3;

                slowdown += SlowdownForArmor.EVENT.invoker().getSlowdownForArmor(player, player.getEquippedStack(slot),
                        slot, slotSlowdown);
            }
            if (slowdown <= 0) {
                if (player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                        .getModifier(armorSlowdownAttribute) != null)
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            .removeModifier(armorSlowdownAttribute);
            } else {
                if (player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                        .getModifier(armorSlowdownAttribute) == null)
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            .addTemporaryModifier(GetSlowdownModifier(slowdown));
            }

            int armorBonus = 0;
            int armorMaxBonus = Integer.MAX_VALUE;
            // If wearing ANY heavy armor, this is 0, otherwise, if wearing ANY medium armor, this is 2.
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                int category = getArmorCategory(player.getEquippedStack(slot), slot);
                if (category == 2) {
                    armorMaxBonus = 0;
                    break;
                } else if (category == 1) {
                    armorMaxBonus = 2;
                }
            }

            // By default, assume Dexterity
            armorBonus = Math.min((data.stats.get(Stat.DEXTERITY) - 10) / 2, armorMaxBonus);
            armorBonus += ArmorBonus.EVENT.invoker().getArmorBonus(player);
            if (armorBonus <= 0) {
                if (player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                        .getModifier(attributeArmorBonus) != null)
                    player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                            .removeModifier(attributeArmorBonus);
            } else {
                if (player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                        .getModifier(attributeArmorBonus) == null)
                    player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR)
                            .addTemporaryModifier(GetArmorBonusModifier(armorBonus));
            }

        }
    }

    public int getArmorCategory(ItemStack armor, EquipmentSlot slot) {
        if (armor.isEmpty())
            return -1;
        if(armor.isIn(PathItemTags.HEAVY_ARMORS))
            return 2;
        if (armor.isIn(PathItemTags.MEDIUM_ARMORS))
            return 1;
        if (armor.isIn(PathItemTags.LIGHT_ARMORS))
            return 0;
        return 1; // Stand-in behaviour, assume medium armor.
    }
}
