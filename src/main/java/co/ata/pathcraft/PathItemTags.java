package co.ata.pathcraft;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class PathItemTags {
    public static final TagKey<Item> PATHCRAFT_ITEMS = TagKey.of(RegistryKeys.ITEM, PathCraft.id("items"));
    public static final TagKey<Item> LIGHT_ARMORS = TagKey.of(RegistryKeys.ITEM, PathCraft.id("light_armors"));
    public static final TagKey<Item> MEDIUM_ARMORS = TagKey.of(RegistryKeys.ITEM, PathCraft.id("medium_armors"));
    public static final TagKey<Item> HEAVY_ARMORS = TagKey.of(RegistryKeys.ITEM, PathCraft.id("heavy_armors"));

}
