package co.ata.pathcraft.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import co.ata.pathcraft.Ancestry;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.PathCraft;
import co.ata.pathcraft.PathCraftRegistries;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.features.LevelFeat;
import co.ata.pathcraft.network.PathNetwork;
import co.ata.pathcraft.Class;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerPathData {
    UUID uuid;
    public int level = 0;
    Ancestry ancestry;
    Class clss;
    ArrayList<FeatureInstance> features = new ArrayList<FeatureInstance>();
    public HashMap<Stat, Integer> stats = new HashMap<Stat, Integer>();
    ServerPathData serverData;

    public boolean dirty = false;

    public PlayerPathData(ServerPathData serverData, UUID uuid) {
        this.serverData = serverData;
        this.uuid = uuid;
        for (Stat stat : Stat.values()) {
            stats.put(stat, 10);
        }
    }

    public void MarkDirty() {
        // Hack. Find all spent Level Up feats and remove them.
        ArrayList<FeatureInstance> featsToRemove = new ArrayList<FeatureInstance>();
        for (FeatureInstance feat : features) {
            if (feat.feature == Feature.LEVEL_FEAT && Feature.LEVEL_FEAT.IsSpent(feat)) {
                featsToRemove.add(feat);
            }
            if(feat.feature == Feature.POINT_BUY && Feature.POINT_BUY.IsSpent(feat)){
                featsToRemove.add(feat);
            }
        }
        for (FeatureInstance feat : featsToRemove) {
            features.remove(feat);
        }


        this.serverData.markDirty();
        this.dirty = true;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putUuid("uuid", uuid);
        nbt.putInt("level", level);

        nbt.putInt("strength", stats.get(Stat.STRENGTH));
        nbt.putInt("dexterity", stats.get(Stat.DEXTERITY));
        nbt.putInt("constitution", stats.get(Stat.CONSTITUTION));
        nbt.putInt("intelligence", stats.get(Stat.INTELLIGENCE));
        nbt.putInt("wisdom", stats.get(Stat.WISDOM));
        nbt.putInt("charisma", stats.get(Stat.CHARISMA));

        if (ancestry != null)
            nbt.putString("ancestry", PathCraftRegistries.ANCESTRY.getId(ancestry).toString());
        if (clss != null)
            nbt.putString("class", PathCraftRegistries.CLASS.getId(clss).toString());
        NbtList featureList = new NbtList();
        for (FeatureInstance feature : features) {
            featureList.add(feature.writeNbtCompound(new NbtCompound()));
        }
        nbt.put("features", featureList);
        return nbt;
    }

    public static PlayerPathData createFromNbt(ServerPathData serverData, NbtCompound nbt) {
        PlayerPathData data = new PlayerPathData(serverData, nbt.getUuid("uuid"));
        data.level = nbt.getInt("level");
        if(nbt.contains("ancestry"))
            data.setAncestry(PathCraftRegistries.ANCESTRY.get(new Identifier(nbt.getString("ancestry"))));
        if (nbt.contains("class"))
            data.setClass(PathCraftRegistries.CLASS.get(new Identifier(nbt.getString("class"))));
        
        data.stats.put(Stat.STRENGTH, nbt.getInt("strength"));
        data.stats.put(Stat.DEXTERITY, nbt.getInt("dexterity"));
        data.stats.put(Stat.CONSTITUTION, nbt.getInt("constitution"));
        data.stats.put(Stat.INTELLIGENCE, nbt.getInt("intelligence"));
        data.stats.put(Stat.WISDOM, nbt.getInt("wisdom"));
        data.stats.put(Stat.CHARISMA, nbt.getInt("charisma"));

        NbtList featureList = nbt.getList("features", NbtList.COMPOUND_TYPE);
        for (int i = 0; i < featureList.size(); i++) {
            data.addFeature(FeatureInstance.createFromNbt(featureList.getCompound(i)));
        }
        return data;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Ancestry getAncestry() {
        return ancestry;
    }

    public void setAncestry(Ancestry ancestry) {
        this.ancestry = ancestry;
        this.MarkDirty();
    }

    public ArrayList<FeatureInstance> getFeatures() {
        return features;
    }

    public void addFeature(FeatureInstance feature) {
        if (!feature.feature.initialized) {
            feature.feature.onInitialize();
            feature.feature.initialized = true;
        }
        features.add(feature);
        if(serverData.server!=null && getPlayer()!=null)
            feature.feature.onAdd(getPlayer(), feature);
        this.MarkDirty();
    }

    public void removeFeature(FeatureInstance feature) {
        features.remove(feature);
        if(getPlayer()!=null)
            feature.feature.onRemove(getPlayer(), feature);
        this.MarkDirty();
    }

    public boolean hasFeature(Feature feature) {
        return features.stream().anyMatch((f) -> f.feature == feature);
    }

    public boolean hasAncestry(Ancestry ancestry) {
        return this.ancestry == ancestry;
    }

    public boolean hasClass(Class clss) {
        return this.clss == clss;
    }

    public Class getClss() {
        return this.clss;
    }

    public void setClass(Class clss) {
        this.clss = clss;
        this.MarkDirty();
    }

    public static PlayerPathData get(MinecraftServer server, UUID uuid) {
        return ServerPathData.get(server).getPlayerData(uuid);
    }

    public static PlayerPathData get(MinecraftServer server, PlayerEntity player) {
        return get(server, player.getUuid());
    }

    public static PlayerPathData get(PlayerEntity player) {
        return get(player.getServer(), player);
    }

    public void LevelUp() {
        level++;
        PathCraft.LOGGER.info("Player " + uuid + " leveled up to level " + level);

        if (level == 1)
            addFeature(new FeatureInstance(Feature.POINT_BUY));

        // Run Feature.onLevelUp for each feature.
        for (FeatureInstance feature : features) {
            feature.feature.onLevelUp(getPlayer(), feature);
        }

        // Every odd level, give the player a feat.
        if (level % 2 == 1) {
            addFeature(LevelFeat.Instance(level));
        }

        if(clss != null)
            for(FeatureInstance ft : clss.getLevelFeatures(level)) {
                addFeature(ft);
            }

        this.MarkDirty();
    }
    
    public void playerLoaded() {
        if (ancestry == null) {
            PathCraft.LOGGER.info("Player " + uuid + " has no ancestry. Asking them to choose one.");
        } else {
            PathCraft.LOGGER.info("Player " + uuid + " has ancestry " + ancestry.toString());
        }
        if (level == 0) {
            LevelUp();
        }
        // Run Feature.onAdd for each feature.
        for (FeatureInstance feature : features) {
            feature.feature.onAdd(getPlayer(), feature);
        }
        this.MarkDirty();
    }

    private ServerPlayerEntity getPlayer() {
        return serverData.server.getPlayerManager().getPlayer(uuid);
    }

    public void sendToClient() {
        PacketByteBuf buf = PacketByteBufs.create();
        // Write Level, ancestry, and features.
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("level", level);
        nbt.putInt("strength", stats.get(Stat.STRENGTH));
        nbt.putInt("dexterity", stats.get(Stat.DEXTERITY));
        nbt.putInt("constitution", stats.get(Stat.CONSTITUTION));
        nbt.putInt("intelligence", stats.get(Stat.INTELLIGENCE));
        nbt.putInt("wisdom", stats.get(Stat.WISDOM));
        nbt.putInt("charisma", stats.get(Stat.CHARISMA));
        if (ancestry != null)
            nbt.putString("ancestry", PathCraftRegistries.ANCESTRY.getId(ancestry).toString());
        if (clss != null)
            nbt.putString("class", PathCraftRegistries.CLASS.getId(clss).toString());
        NbtList featureList = new NbtList();
        for (FeatureInstance feature : features) {
            featureList.add(feature.writeNbtCompound(new NbtCompound()));
        }
        nbt.put("features", featureList);
        buf.writeNbt(nbt);
        
        ServerPlayNetworking.send(getPlayer(), PathNetwork.UpdatePathData, buf);
    }

    public void Respec(){
        this.level = 0;
        this.ancestry = null;
        this.clss = null;
        this.stats.put(Stat.STRENGTH, 10);
        this.stats.put(Stat.DEXTERITY, 10);
        this.stats.put(Stat.CONSTITUTION, 10);
        this.stats.put(Stat.INTELLIGENCE, 10);
        this.stats.put(Stat.WISDOM, 10);
        this.stats.put(Stat.CHARISMA, 10);
        this.features.clear();
        this.LevelUp();
    }

}
