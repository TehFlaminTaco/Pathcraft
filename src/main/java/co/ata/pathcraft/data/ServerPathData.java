package co.ata.pathcraft.data;

import java.util.HashMap;
import java.util.UUID;

import co.ata.pathcraft.PathCraft;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class ServerPathData extends PersistentState {

    HashMap<UUID, PlayerPathData> playerData = new HashMap<UUID, PlayerPathData>();
    MinecraftServer server;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList playerList = new NbtList();
        for (UUID uuid : playerData.keySet()) {
            playerList.add(playerData.get(uuid).writeNbt(new NbtCompound()));
        }
        nbt.put("players", playerList);
        return nbt;
    }

    public PlayerPathData getPlayerData(UUID uuid) {
        if (!playerData.containsKey(uuid)) {
            playerData.put(uuid, new PlayerPathData(this, uuid));
        }
        return playerData.get(uuid);
    }

    private static ServerPathData createFromNbt(NbtCompound nbt) {
        ServerPathData data = new ServerPathData();
        NbtList playerList = nbt.getList("players", NbtList.COMPOUND_TYPE);
        for (int i = 0; i < playerList.size(); i++) {
            PlayerPathData playerData = PlayerPathData.createFromNbt(data, playerList.getCompound(i));
            data.playerData.put(playerData.getUUID(), playerData);
        }
        return data;
    }

    public static ServerPathData get(MinecraftServer server) {
        PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
        ServerPathData data = manager.getOrCreate(
                ServerPathData::createFromNbt,
                ServerPathData::new,
                PathCraft.MOD_ID);
        data.server = server;
        
        data.markDirty();

        return data;
    }

    
}
