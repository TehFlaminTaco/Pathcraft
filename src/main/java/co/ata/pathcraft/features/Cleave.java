package co.ata.pathcraft.features;

import java.util.ArrayList;
import java.util.List;

import co.ata.pathcraft.Category;
import co.ata.pathcraft.Feature;
import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.FeatureType;
import co.ata.pathcraft.Prerequisites;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.data.PlayerPathData;
import co.ata.pathcraft.events.MonsterKilled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

public class Cleave extends Feature implements MonsterKilled {
    public Cleave() {
        type = FeatureType.COMBAT;
    }

    @Override
    public void onInitialize() {
        MonsterKilled.EVENT.register(this);
    }

    @Override
    public void onMonsterKilled(PlayerEntity player, LivingEntity monster) {
        if (player.world.isClient)
            return;

        PlayerPathData data = PlayerPathData.get(player.getServer(), player);
        if (data.hasFeature(this)) {
            // Re-attack a random nearby HOSTILE target
            player.world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(3.0f), (e) -> {
                return e != monster && e != player && e.isAttackable() && e.isAlive() && e instanceof Monster;
            }).stream().findAny().ifPresent((e) -> {
                //player.getItemCooldownManager().set(player.getActiveItem().getItem(), 0);
                player.resetLastAttackedTicks();
                player.attack(e);
            });
        }
    }
    
    @Override
    public List<FeatureInstance> Selectable(String category) {
        List<FeatureInstance> list = new ArrayList<FeatureInstance>();
        if (Category.Is(category, "general,combat"))
            list.add(new FeatureInstance(this));
        return list;
    }
    
    @Override
    public boolean meetsPrerequisites(PlayerEntity player, FeatureInstance instance) {
        return Prerequisites.begin()
                    .HasStat(player, Stat.STRENGTH, 13)
                    .get();
    }
}
