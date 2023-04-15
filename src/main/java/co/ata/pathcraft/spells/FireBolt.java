package co.ata.pathcraft.spells;

import co.ata.pathcraft.Spell;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FireBolt extends Spell {
    public FireBolt() {
        super();
        this.school = School.Evocation;
        this.range = Range.Close;
        this.shape = Shape.Single;

        RayParticle = ParticleTypes.FLAME;
    }

    @Override
    public void UseOnBlock(HitResult hit, World world, BlockPos pos, PlayerEntity caster) {
        // Light the hit block on fire
        if (world.getBlockState(pos).isReplaceable()) {
            world.setBlockState(BlockPos.ofFloored(hit.getPos()), Blocks.FIRE.getDefaultState());
        }
    }

    @Override
    public void UseOnEntity(HitResult hit, Entity target, PlayerEntity caster) {
        target.damage(
                caster.getDamageSources().magic(),
                1.0f + this.GetBonusDamage(caster));
        target.setOnFireFor(1);
    }
    
}
