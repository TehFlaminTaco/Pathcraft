package co.ata.pathcraft;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

public class PathCraftRegistries {
    public static final SimpleRegistry<Feature> FEATURE;
    public static final SimpleRegistry<Ancestry> ANCESTRY;
    public static final SimpleRegistry<Class> CLASS;
    public static final SimpleRegistry<Ability> ABILITY;

    static {
        FEATURE = FabricRegistryBuilder
                .createSimple(RegistryKey.<Feature>ofRegistry(PathCraft.id("feature")))
                .attribute(RegistryAttribute.SYNCED).buildAndRegister();
        ANCESTRY = FabricRegistryBuilder
                .createSimple(RegistryKey.<Ancestry>ofRegistry(PathCraft.id("ancestry")))
                .attribute(RegistryAttribute.SYNCED).buildAndRegister();
        CLASS = FabricRegistryBuilder
                .createSimple(RegistryKey.<Class>ofRegistry(PathCraft.id("class")))
                .attribute(RegistryAttribute.SYNCED).buildAndRegister();
        ABILITY = FabricRegistryBuilder
                .createSimple(RegistryKey.<Ability>ofRegistry(PathCraft.id("ability")))
                .attribute(RegistryAttribute.SYNCED).buildAndRegister();
    }
}
