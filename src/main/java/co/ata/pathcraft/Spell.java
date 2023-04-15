package co.ata.pathcraft;

import co.ata.pathcraft.data.PlayerPathData;
import co.ata.pathcraft.spells.FireBolt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class Spell extends Ability {
    public static final FireBolt FIREBOLT = new FireBolt();

    public enum School {
        Evocation,
        Conjuration,
        Abjuration,
        Enchantment,
        Illusion,
        Necromancy,
        Transmutation,
        Divination
    }

    public enum Range {
        Personal,
        Touch,
        Close,
        Medium,
        Long,
        Unlimited
    }

    public enum Shape {
        Single,
        Cone,
        Line,
        Burst
    }

    public int slotLevel = 0;
    public School school;
    public Shape shape;
    public Range range;

    public ParticleEffect RayParticle = null;
    public ParticleEffect AreaParticle = null;

    public abstract void UseOnBlock(HitResult hit, World world, BlockPos pos, PlayerEntity caster);

    public abstract void UseOnEntity(HitResult hit, Entity target, PlayerEntity caster);
    
    @Override
    public boolean CanUse(AbilityInstance instance, PlayerEntity user) {
        // TODO: Has Slot for Level
        return true;
    }

    @Override
    public boolean Use(AbilityInstance instance, PlayerEntity user) {
        // Load the spell unto the player's Spell Focus.
        return true; // Spend a use.
    }

    public int GetBonusDamage(PlayerEntity caster) {
        int statAmount = 0;
        if(caster.world.isClient)
            statAmount = PathCraftClient.data.stats.get(PathCraftClient.data.clss.primaryStat);
        else
            statAmount = PlayerPathData.get(caster).stats.get(PlayerPathData.get(caster).getClss().primaryStat);

        return (int)((statAmount - 10) / 2);
    }

    public void Cast(PlayerEntity caster) {
        int distance = 0;
        int casterLevel = 0;
        if(caster.world.isClient)
            casterLevel = PathCraftClient.data.level;
        else
            casterLevel = PlayerPathData.get(caster).level;

        switch (this.range) {
            case Personal:
                distance = 0;
                break;
            case Touch:
                distance = 15;
                break;
            case Close:
                distance = 25 + 5 * (casterLevel / 2);
                break;
            case Medium:
                distance = 100 + 10 * casterLevel;
                break;
            case Long:
                distance = 400 + 40 * casterLevel;
                break;
            case Unlimited:
                distance = 10000;
                break;
        }
        float distMeters = distance / 3.28084f;
        Vec3d cameraPos = caster.getCameraPosVec(1f);
        Vec3d camDir = caster.getRotationVec(1f);

        EntityHitResult entityHit = ProjectileUtil.raycast(
            caster,
            cameraPos,
            cameraPos.add(camDir.multiply(distMeters)), 
            caster.getBoundingBox().stretch(camDir.multiply(distMeters)).expand(1.0D),
            (entity) -> !entity.isSpectator(),
            distMeters * distMeters
        );

        HitResult hit = caster.raycast(distMeters, 0, false);
        if(entityHit != null)
            hit = entityHit;

        if (this.RayParticle != null && caster.world.isClient) {
            //PathCraft.LOGGER.info("Has RayParticle");
            // For every point between the caster and the hit, spawn a particle.
            double x = caster.getX();
            double y = caster.getY() + caster.getStandingEyeHeight() - 0.1;
            double z = caster.getZ();
            double dx = hit.getPos().x - x;
            double dy = hit.getPos().y - y;
            double dz = hit.getPos().z - z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double step = 0.1;
            for (double i = 0; i < dist; i += step) {
                float randomX = (float) (Math.random() - 0.5) * 0.1f;
                float randomY = (float) (Math.random() - 0.5) * 0.1f;
                float randomZ = (float) (Math.random() - 0.5) * 0.1f;
                //PathCraft.LOGGER.info("Spawning particle @ " + (x + dx * i / dist) + ", " + (y + dy * i / dist) + ", " + (z + dz * i / dist));
                caster.world.addParticle(
                    this.RayParticle,
                    x + dx * i / dist,
                    y + dy * i / dist,
                    z + dz * i / dist,
                    randomX, randomY, randomZ
                );
            }

        }

        switch (this.shape) {
            case Single:
                if (this.range == Range.Personal) {
                    // Cast on self
                    this.UseOnEntity(hit, caster, caster);
                } else {
                    // Cast on block or entity
                    if (hit.getType() == HitResult.Type.ENTITY) {
                        this.UseOnEntity(hit, ((EntityHitResult) hit).getEntity(), caster);
                    } else {
                        this.UseOnBlock(hit, caster.world, ((BlockHitResult) hit).getBlockPos(), caster);
                    }
                }
                break;
            case Cone:
                // Every block and entity in a cone in front of the caster.
                break;
            case Line:
                // Every block and entity in a line in front of the caster.
                break;
            case Burst:
                // Every block and entity in a sphere around the caster.
                break;
        }
    }
}
