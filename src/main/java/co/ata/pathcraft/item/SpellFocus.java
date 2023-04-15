package co.ata.pathcraft.item;

import co.ata.pathcraft.PathCraftRegistries;
import co.ata.pathcraft.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SpellFocus extends Item {

    public SpellFocus(Settings settings) {
        super(settings);
    }
    
    public Spell getLoadedSpell(ItemStack stack) {
        if(stack.getNbt() != null && stack.getNbt().contains("pathcraft:spell"))
            return (Spell)PathCraftRegistries.ABILITY.get(new Identifier(stack.getNbt().getString("pathcraft:spell")));
        return null;
    }
    
    public void setLoadedSpell(ItemStack stack, Spell spell) {
        if (spell == null)
            stack.getOrCreateNbt().remove("pathcraft:spell");
        else
            stack.getOrCreateNbt().putString("pathcraft:spell", PathCraftRegistries.ABILITY.getId(spell).toString());
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        return getLoadedSpell(stack) != null || super.hasGlint(stack);
    }

    public void Fizzle(ItemStack stack, World world, Entity entity) {
        if (getLoadedSpell(stack) != null) {
            setLoadedSpell(stack, null);
            world.playSound(entity, entity.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,
                    1.0f, 1.0f);
        }
    }

    @Override
    public boolean isNbtSynced() {
        return true;
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, net.minecraft.util.Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        // For testing, just load the firebolt spell.
        Spell loaded = this.getLoadedSpell(stack);
        if(loaded == null)
            this.setLoadedSpell(stack, Spell.FIREBOLT);
        else {
            loaded.Cast(user);
            this.setLoadedSpell(stack, null);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, java.util.List<net.minecraft.text.Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        Spell spell = getLoadedSpell(stack);
        if(spell != null)
            tooltip.add(Text.translatable("item.pathcraft.spell_focus.tooltip.loaded", spell.getName()));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        // If spell is loaded and this isn't the mainhand item, unload it.
        if (!(entity instanceof PlayerEntity)) {
            Fizzle(stack, world, entity);
            return;
        }
        PlayerEntity player = (PlayerEntity) entity;
        if(player.getMainHandStack() != stack && player.getOffHandStack() != stack)
            Fizzle(stack, world, entity);
        
    }


}
