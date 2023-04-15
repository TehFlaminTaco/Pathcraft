package co.ata.pathcraft.network;

import co.ata.pathcraft.PathCraftClient;
import co.ata.pathcraft.data.ClientPathData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;

public class ClientNetwork {
    public static void Register() {
        ClientPlayNetworking.registerGlobalReceiver(PathNetwork.UpdatePathData, (client, handler, buf, responseSender) -> {
            // Load a ClientPathData object from the buffer
            // Update the client's path data

            NbtCompound nbt = buf.readNbt();
            PathCraftClient.data = ClientPathData.createFromNbt(nbt);

            PathCraftClient.NextCharacterGenTick = 0;
            // Send a test message to the client's chat
            //client.inGameHud.getChatHud().addMessage(Text.of("Client data loaded! You are a level " + PathCraftClient.data.level + " " + PathCraftClient.data.ancestry.getName().getString()));
        });
    }

}
