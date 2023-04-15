package co.ata.pathcraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import co.ata.pathcraft.events.MonsterKilled;
import co.ata.pathcraft.events.TakeDamage;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntity.class)
public class MonsterMixins {

    @Inject(at = @At("HEAD"), method = "onKilledBy(Lnet/minecraft/entity/LivingEntity;)V")
    protected void onKilledBy(LivingEntity adversary, CallbackInfo info) {
        if (adversary instanceof PlayerEntity && !adversary.world.isClient) {
            MonsterKilled.EVENT.invoker().onMonsterKilled((PlayerEntity) adversary, (LivingEntity) (Object) this);
        }
    }

    @Inject(at = @At("HEAD"), method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        TakeDamage.EVENT.invoker().onTakeDamage((LivingEntity) (Object) this, source, amount);
    }
}
