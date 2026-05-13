package com.kasetoatz.jukeboxboat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin
{
    @ModifyReturnValue(method="assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;", at=@At("RETURN"))
    public ItemStack craft(ItemStack original, @Local(argsOnly=true, name="input") CraftingInput input)
    {
        if (!original.isEmpty() && input.items().stream().anyMatch(stack -> stack.getHoverName().contains(Component.translatable("jukeboxboat.item.name"))))
        {
            return ItemStack.EMPTY;
        }
        if (((ShapelessRecipe)(Object)this).group().equals("jukebox_boat"))
        {
            original.set(DataComponents.ITEM_NAME, Component.translatable("jukeboxboat.item.name"));
        }
        return original;
    }
}
