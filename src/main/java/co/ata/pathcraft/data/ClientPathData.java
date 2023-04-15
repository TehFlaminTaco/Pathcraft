package co.ata.pathcraft.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.ata.pathcraft.Ancestry;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.PathCraft;
import co.ata.pathcraft.PathCraftClient;
import co.ata.pathcraft.PathCraftRegistries;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.client.PointBuyScreen;
import co.ata.pathcraft.client.SelectOption;
import co.ata.pathcraft.client.SelectScreen;
import co.ata.pathcraft.features.BonusFeat;
import co.ata.pathcraft.network.PathNetwork;
import co.ata.pathcraft.Class;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientPathData {
    public int level;
    public Ancestry ancestry;
    public Class clss;
    public ArrayList<FeatureInstance> features = new ArrayList<FeatureInstance>();
    public HashMap<Stat, Integer> stats = new HashMap<Stat, Integer>();

    public static ClientPathData createFromNbt(NbtCompound nbt) {
        ClientPathData data = new ClientPathData();
        data.level = nbt.getInt("level");
        if (nbt.contains("ancestry"))
            data.ancestry = PathCraftRegistries.ANCESTRY
                    .get(new Identifier(nbt.getString("ancestry")));
        if (nbt.contains("class"))
            data.clss = PathCraftRegistries.CLASS.get(new Identifier(nbt.getString("class")));
        NbtList featureList = nbt.getList("features", NbtList.COMPOUND_TYPE);
        for (int i = 0; i < featureList.size(); i++) {
            data.features.add(FeatureInstance.createFromNbt(featureList.getCompound(i)));
        }
        data.stats.put(Stat.STRENGTH, nbt.getInt("strength"));
        data.stats.put(Stat.DEXTERITY, nbt.getInt("dexterity"));
        data.stats.put(Stat.CONSTITUTION, nbt.getInt("constitution"));
        data.stats.put(Stat.INTELLIGENCE, nbt.getInt("intelligence"));
        data.stats.put(Stat.WISDOM, nbt.getInt("wisdom"));
        data.stats.put(Stat.CHARISMA, nbt.getInt("charisma"));
        return data;
    }

    public void RequestAncestry() {
        ArrayList<SelectOption<Ancestry>> options = new ArrayList<SelectOption<Ancestry>>();
        for (Ancestry a : PathCraftRegistries.ANCESTRY) {
            options.add(new SelectOption<Ancestry>(a, a.getName(), a.getDescription()));
        }
        PathCraftClient.MC.setScreen(
                new SelectScreen<Ancestry>(Text.translatable("screen.pathcraft:select.ancestries"), options, (a) -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeIdentifier(PathCraftRegistries.ANCESTRY.getId(a));
                    ClientPlayNetworking.send(PathNetwork.SetAncestry, buf);
                    this.ancestry = a;
                    PathCraftClient.NextCharacterGenTick = 20 * 5;
                }));
    }

    public void RequestClass() {
        ArrayList<SelectOption<Class>> options = new ArrayList<SelectOption<Class>>();
        for (Class c : PathCraftRegistries.CLASS) {
            options.add(new SelectOption<Class>(c, c.getName(), c.getDescription()));
        }
        PathCraftClient.MC.setScreen(
                new SelectScreen<Class>(Text.translatable("screen.pathcraft:select.classes"), options, (c) -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeIdentifier(PathCraftRegistries.CLASS.getId(c));
                    ClientPlayNetworking.send(PathNetwork.SetClass, buf);
                    this.clss = c;
                    PathCraftClient.NextCharacterGenTick = 20 * 5;
                }));
    }

    public void RequestFeat(FeatureInstance bonusFeat) {
        ArrayList<SelectOption<FeatureInstance>> options = new ArrayList<SelectOption<FeatureInstance>>();
        String cat = ((BonusFeat) bonusFeat.feature).Category;
        List<FeatureInstance> selectable = new ArrayList<FeatureInstance>();
        for (Feature f : PathCraftRegistries.FEATURE) {
            PathCraft.LOGGER.info("LOADING SELECTABLES FROM: " + f.getClass().getSimpleName());
            List<FeatureInstance> feats = f.Selectable(cat);
            selectable.addAll(feats);
            PathCraft.LOGGER.info("LOADED " + feats.size() + " FEATS! (" + selectable.size() + ")");

        }
        selectable.removeIf(c->PathCraftClient.data.features.stream().anyMatch(b->c.feature.InstanceEquals(c, b)));
        // Sort by Meeting pre-requisets, then by name
        selectable.sort((a, b) -> {
            boolean aMeets = a.feature.meetsPrerequisites(a);
            boolean bMeets = b.feature.meetsPrerequisites(b);
            if (aMeets && !bMeets)
                return -1;
            if (!aMeets && bMeets)
                return 1;
            return a.feature.getName(a).getString().compareTo(b.feature.getName(b).getString());
        });
        for (FeatureInstance f : selectable) {
            SelectOption<FeatureInstance> option = new SelectOption<FeatureInstance>(f, f.feature.getName(f),
                Text.of(f.feature.getPrerequisites(f).getString() + "\n" + f.feature.getDescription(f).getString()));
            if (!f.feature.meetsPrerequisites(f))
                option.disabled = true;
            options.add(option);
        }
        PathCraftClient.MC.setScreen(
            new SelectScreen<FeatureInstance>(
                bonusFeat.getName(),
                options,
                (f) -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeNbt(f.writeNbtCompound(new NbtCompound()));
                    ClientPlayNetworking.send(PathNetwork.SetFeature, buf);
                    this.features.add(f);
                    ((BonusFeat) bonusFeat.feature).Spend(bonusFeat);
                    PathCraftClient.NextCharacterGenTick = 20 * 5;
                }
        ));
    }

    public void RequestPointBuy() {
        PathCraftClient.MC.setScreen(new PointBuyScreen());
    }
}
