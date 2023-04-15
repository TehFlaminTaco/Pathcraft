package co.ata.pathcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.ata.pathcraft.data.PlayerPathData;
import co.ata.pathcraft.gameplay.ArmorMods;
import co.ata.pathcraft.item.SpellFocus;
import co.ata.pathcraft.network.ServerNetwork;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;

public class PathCraft implements ModInitializer {
	public static final String MOD_ID = "pathcraft";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerNetwork.Register();
		ArmorMods.init();

		// Register for the Ancestry Registry
		Registry.register(PathCraftRegistries.ANCESTRY, id("human"), Ancestry.HUMAN);
		Registry.register(PathCraftRegistries.ANCESTRY, id("elf"), Ancestry.ELF);

		// Register for the Feature Registry
		//bonusfeat
			Registry.register(PathCraftRegistries.FEATURE, id("bonusfeat"), Feature.BONUS_FEAT);
		//level_feat
			Registry.register(PathCraftRegistries.FEATURE, id("level_feat"), Feature.LEVEL_FEAT);
		//combat_bonus_feat
			Registry.register(PathCraftRegistries.FEATURE, id("combat_bonus_feat"), Feature.COMBAT_BONUS_FEAT);
		//point_buy
			Registry.register(PathCraftRegistries.FEATURE, id("point_buy"), Feature.POINT_BUY);
		//cleave
			Registry.register(PathCraftRegistries.FEATURE, id("cleave"), Feature.CLEAVE);
		//weapon_focus
			Registry.register(PathCraftRegistries.FEATURE, id("weapon_focus"), Feature.WEAPON_FOCUS);
		//armor_proficiency
			Registry.register(PathCraftRegistries.FEATURE, id("armor_proficiency"), Feature.ARMOR_PROFICIENCY);
		//two_weapon_fighting
			Registry.register(PathCraftRegistries.FEATURE, id("two_weapon_fighting"), Feature.TWO_WEAPON_FIGHTING);
		//elven_magic
			Registry.register(PathCraftRegistries.FEATURE, id("elven_magic"), Feature.ELVEN_MAGIC);
		//elven_quickness
			Registry.register(PathCraftRegistries.FEATURE, id("elven_quickness"), Feature.ELVEN_QUICKNESS);
		//armor_training
			Registry.register(PathCraftRegistries.FEATURE, id("armor_training"), Feature.ARMOR_TRAINING);
		//weapon_training
			Registry.register(PathCraftRegistries.FEATURE, id("weapon_training"), Feature.WEAPON_TRAINING);
		//sneak_attack
			Registry.register(PathCraftRegistries.FEATURE, id("sneak_attack"), Feature.SNEAK_ATTACK);
		//cantrips	
			Registry.register(PathCraftRegistries.FEATURE, id("cantrips"), Feature.CANTRIPS);

		// Register for the Class Registry
		Registry.register(PathCraftRegistries.CLASS, id("fighter"), Class.FIGHTER);
		Registry.register(PathCraftRegistries.CLASS, id("rogue"), Class.ROGUE);
		Registry.register(PathCraftRegistries.CLASS, id("wizard"), Class.WIZARD);
	
		// Register for the Ability Registry
		// Abilities

		// Spells
		//firebolt
			Registry.register(PathCraftRegistries.ABILITY, id("firebolt"), Spell.FIREBOLT);

		// ITEMS :D
		Registry.register(Registries.ITEM, id("wand"), new SpellFocus(
				new FabricItemSettings()
					.maxCount(1)
			)
		);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PlayerPathData.get(server, handler.player.getUuid())
					.playerLoaded();
		});

		// On tick event
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// Iterate over all players
			for (var player : server.getPlayerManager().getPlayerList()) {
				// Get the player's data
				var data = PlayerPathData.get(server, player.getUuid());
				// If it's dirty, send it.
				if (data.dirty) {
					data.sendToClient();
					data.dirty = false;
				}
			}
		});

		// Commands
		CommandRegistrationCallback.EVENT
				.register((dispatcher, registryAccess, evironment) -> {
					dispatcher.register(
						literal("respec")
						.requires(source -> source.hasPermissionLevel(4))
								.then(argument("target", EntityArgumentType.players())
								.executes(c -> {
									Collection<ServerPlayerEntity> players = EntityArgumentType
											.getPlayers(c, "target");
									for (ServerPlayerEntity player : players) {
										var data = PlayerPathData.get(player);
										data.Respec();
									}
									return 1;
								})));
					dispatcher.register(
						literal("levelup")
						.requires(source -> source.hasPermissionLevel(4))
								.then(argument("target", EntityArgumentType.players())
								.executes(c -> {
									Collection<ServerPlayerEntity> players = EntityArgumentType
											.getPlayers(c, "target");
									for (ServerPlayerEntity player : players) {
										var data = PlayerPathData.get(player);
										data.LevelUp();
									}
									return 1;
								})));
				});
		
				
	}
	
	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
