package co.ata.pathcraft.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import co.ata.pathcraft.Category;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.FeatureType;
import co.ata.pathcraft.data.PlayerPathData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

public class WeaponFocus extends Feature {
    public WeaponFocus(){
        type = FeatureType.COMBAT;
    }

    public String GetWeaponClass(FeatureInstance instance) {
        if (instance.data.contains("weaponclass"))
            return instance.data.getString("weaponclass");
        return "";
    }

    public static FeatureInstance Instance(String weaponclass) {
        FeatureInstance instance = new FeatureInstance(Feature.WEAPON_FOCUS);
        instance.data.putString("weaponclass", weaponclass);
        return instance;
    }

    public static HashMap<String, TagKey<Item>> WeaponClasses = new HashMap<String, TagKey<Item>>();

    public static UUID weaponFocusAttribute = UUID.fromString("8e51c6dd-5b9b-452e-8f7c-29488088d26b");

    public EntityAttributeModifier GetModifier(FeatureInstance instance) {
        return new EntityAttributeModifier(weaponFocusAttribute, "pathcraft:weaponfocus", 1d, EntityAttributeModifier.Operation.ADDITION);
    }


    public static String GetWeaponClass(ItemStack stack) {
        String weaponclass = "";

        if (stack.getItem() instanceof TridentItem) {
            weaponclass = "trident";
        } else if (stack.getItem() instanceof BowItem) {
            weaponclass = "bow";
        } else if (stack.getItem() instanceof CrossbowItem) {
            weaponclass = "crossbow";
        } else {
            for (String key : WeaponClasses.keySet()) {
                if (stack.isIn(WeaponClasses.get(key))) {
                    weaponclass = key;
                    break;
                }
            }
        }
        return weaponclass;
    }
    @Override
    public void onInitialize() {
        WeaponClasses.put("axe", ItemTags.AXES);
        WeaponClasses.put("sword", ItemTags.SWORDS);
        WeaponClasses.put("pickaxe", ItemTags.PICKAXES);
        WeaponClasses.put("shovel", ItemTags.SHOVELS);
        WeaponClasses.put("hoe", ItemTags.HOES);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerPathData data = PlayerPathData.get(server, player);
                if (data == null) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).removeModifier(weaponFocusAttribute);
                    continue;
                }
                // Get the weapon class of the selected item
                String weaponclass = GetWeaponClass(player.getMainHandStack());
                if (weaponclass == "") {
                    player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                            .removeModifier(weaponFocusAttribute);
                    continue;
                }
                
                boolean hasFocus = false;
                // Check if the player has a weapon focus with this class
                for (FeatureInstance instance : data.getFeatures()) {
                    if (instance.feature == Feature.WEAPON_FOCUS && GetWeaponClass(instance).equals(weaponclass)) {
                        // Add the weapon focus bonus
                        hasFocus = true;
                        if(!player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).hasModifier(GetModifier(instance)))
                            player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                                .addTemporaryModifier(this.GetModifier(instance));
                        break;
                    }
                }
                if(!hasFocus)
                    player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                        .removeModifier(weaponFocusAttribute);
            }
        });
    }

    @Override
    public Text getName(FeatureInstance instance) {
        return Text.translatable("feature.pathcraft:weapon_focus.name", GetWeaponClass(instance));
    }

    @Override
    public Text getDescription(FeatureInstance instance) {
        return Text.translatable("feature.pathcraft:weapon_focus.description", GetWeaponClass(instance));
    }

    @Override
    public List<FeatureInstance> Selectable(String category) {
        List<FeatureInstance> list = new ArrayList<FeatureInstance>();
        if (Category.Is(category, "general,combat")) {
            list.add(Instance("axe"));
            list.add(Instance("sword"));
            list.add(Instance("pickaxe"));
            list.add(Instance("shovel"));
            list.add(Instance("hoe"));
            list.add(Instance("bow"));
            list.add(Instance("crossbow"));
            list.add(Instance("trident"));
        }
        return list;
    }

    @Override
    public boolean InstanceEquals(FeatureInstance a, FeatureInstance b) {
        return a.feature == b.feature && GetWeaponClass(a).equals(GetWeaponClass(b));
    }
}
