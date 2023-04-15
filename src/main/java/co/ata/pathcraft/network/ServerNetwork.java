package co.ata.pathcraft.network;

import java.util.List;

import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.PathCraft;
import co.ata.pathcraft.PathCraftRegistries;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.data.PlayerPathData;
import co.ata.pathcraft.features.BonusFeat;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class ServerNetwork {
    public static void Register() {
        ServerPlayNetworking.registerGlobalReceiver(PathNetwork.SetAncestry, (server, player, handler, buf, responseSender) -> {
            PlayerPathData data = PlayerPathData.get(server, player);
            PathCraft.LOGGER.info("Received ancestry update from " + player.getName().getString() + "");
            if (data.getAncestry() != null)
                return; // Ignore double ups;
            // Get an identifier from the buffer
            Identifier id = buf.readIdentifier();
            // Set the ancestry
            PathCraft.LOGGER.info("Updating ancestry to " + id.toString() + "");
            data.setAncestry(PathCraftRegistries.ANCESTRY.get(id));
            // Add all the ancestry features
            for (FeatureInstance feature : data.getAncestry().getFeatures()) {
                data.addFeature(feature);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PathNetwork.SetClass, (server, player, handler, buf, responseSender) -> {
            PlayerPathData data = PlayerPathData.get(server, player);
            PathCraft.LOGGER.info("Received class update from " + player.getName().getString() + "");
            if (data.getClss() != null)
                return; // Ignore double ups;
            // Get an identifier from the buffer
            Identifier id = buf.readIdentifier();
            // Set the ancestry
            PathCraft.LOGGER.info("Updating class to " + id.toString() + "");
            data.setClass(PathCraftRegistries.CLASS.get(id));
            // Add all the ancestry features
            for (FeatureInstance feature : data.getClss().getLevelFeatures(1)) {
                data.addFeature(feature);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PathNetwork.SetFeature, (server, player, handler, buf, responseSender) -> {
            PlayerPathData data = PlayerPathData.get(server, player);
            PathCraft.LOGGER.info("Received feature update from " + player.getName().getString() + "");
            // Try to find a feature that this was meant to overwrite
            for (FeatureInstance fi : data.getFeatures()) {
                if (fi.feature instanceof BonusFeat) {
                    BonusFeat bf = (BonusFeat) fi.feature;
                    if (bf.IsSpent(fi))
                        continue;
                    
                    // THIS is the feature we want to overwrite
                    // Read a tag compound from the buffer
                    NbtCompound nbt = buf.readNbt();
                    // Check if that tag compound was produced by the feature
                    List<FeatureInstance> generated;
                    for(Feature feat : PathCraftRegistries.FEATURE) {
                        generated = feat.Selectable("*");
                        for(FeatureInstance gen : generated) {
                            if(gen.writeNbtCompound(new NbtCompound()).equals(nbt) && bf.CanAcceptFeat(fi, gen) && gen.meetsPrerequisites(player)) {
                                // Overwrite the feature
                                bf.Spend(fi);
                                data.addFeature(gen);
                                break;
                            }
                        }
                        if(bf.IsSpent(fi))
                            break;
                    }
                    if(bf.IsSpent(fi))
                        break;
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PathNetwork.SetStats, (server, player, handler, buf, responseSender) -> {
            PlayerPathData data = PlayerPathData.get(server, player);
            PathCraft.LOGGER.info("Received stat update from " + player.getName().getString() + "");
            // Confirm they have an unspent PointBuy feature
            if (!data.getFeatures().stream()
                    .anyMatch(c -> c.feature == Feature.POINT_BUY && !Feature.POINT_BUY.IsSpent(c))) {
                return;
            }
            
            FeatureInstance pointBuy = data.getFeatures().stream()
                    .filter(c -> c.feature == Feature.POINT_BUY && !Feature.POINT_BUY.IsSpent(c)).findFirst().get();
            
            for (Stat stat : Stat.values()) {
                int value = buf.readInt();
                if(data.getAncestry().RacialStats.containsKey(stat))
                    value += data.getAncestry().RacialStats.get(stat);
                data.stats.put(stat, value);
            }
            Feature.POINT_BUY.Spend(pointBuy);
            data.MarkDirty();
        });
    }
}
