package co.ata.pathcraft;

import org.lwjgl.glfw.GLFW;

import co.ata.pathcraft.client.AbilityBookScreen;
import co.ata.pathcraft.client.CharacterScreen;
import co.ata.pathcraft.data.ClientPathData;
import co.ata.pathcraft.features.BonusFeat;
import co.ata.pathcraft.features.PointBuy;
import co.ata.pathcraft.network.ClientNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class PathCraftClient implements ClientModInitializer {
    public static ClientPathData data = null;
    public static MinecraftClient MC;

    public static int NextCharacterGenTick = 0;

    private static KeyBinding openCharacterSheet;
    private static KeyBinding openAbilityBook;

    static {
        openCharacterSheet = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pathcraft:open_character_sheet",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.pathcraft"
        ));
        openAbilityBook = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pathcraft:open_ability_book",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.pathcraft"
        ));
    }
    
    
    @Override
    public void onInitializeClient() {
        ClientNetwork.Register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openCharacterSheet.wasPressed()) {
                if (data != null && data.ancestry != null && data.clss != null)
                    client.setScreen(new CharacterScreen());
            }
            if (openAbilityBook.wasPressed()) {
                if (data != null && data.ancestry != null && data.clss != null)
                    client.setScreen(new AbilityBookScreen());
            }
            
            if(NextCharacterGenTick > 0)
                NextCharacterGenTick--;
            if (MC.currentScreen == null) {
                if (data != null && NextCharacterGenTick <= 0) {
                    // Check for Ancestry
                    if (data.ancestry == null) {
                        data.RequestAncestry();
                        return;
                    }

                    if (data.clss == null) {
                        data.RequestClass();
                        return;
                    }
                    // Check for unspent instanceof a Point Buy
                    for(FeatureInstance fi : data.features){
                        if(fi.feature instanceof PointBuy){
                            if(((PointBuy)fi.feature).IsSpent(fi))
                                continue;
                            // Ask to select points.
                            data.RequestPointBuy();
                            return;
                        }
                    }

                    // Check for instances of BonusFeat that aren't spent, and if any exist, ask to select a new feat.
                    for (FeatureInstance fi : data.features) {
                        if (fi.feature instanceof BonusFeat) {
                            if(((BonusFeat)fi.feature).IsSpent(fi))
                                continue;
                            // Ask to select a new feat.
                            data.RequestFeat(fi);
                            return;
                        }
                        
                    }
                }
            }
        });

        MC = MinecraftClient.getInstance();
    }
    
}
