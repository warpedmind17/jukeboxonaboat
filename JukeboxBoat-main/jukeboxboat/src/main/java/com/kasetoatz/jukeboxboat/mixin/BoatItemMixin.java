package com.kasetoatz.jukeboxboat.mixin;

import com.kasetoatz.jukeboxboat.interfaces.IAbstractBoat;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.kasetoatz.jukeboxboat.util.Util.createJukebox;
import static com.kasetoatz.jukeboxboat.util.Util.spawnPlaceholder;

@Mixin(BoatItem.class)
public class BoatItemMixin
{
    @Inject(method="use", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;setYRot(F)V"))
    public void use(CallbackInfoReturnable<InteractionResult> cir, @Local(name="itemStack") ItemStack itemStack, @Local(name="boat") AbstractBoat boat)
    {
        if (boat instanceof Boat jukebox)
        {
            boolean isJukeboxBoat = false;
            Component name = itemStack.get(DataComponents.ITEM_NAME);
            if (name != null)
            {
                isJukeboxBoat = name.contains(Component.translatable("jukeboxboat.item.name"));
            }
            ((IAbstractBoat)boat).jukeboxBoat$setJukeboxBoat(isJukeboxBoat);
            if (!isJukeboxBoat)
            {
                return;
            }
            spawnPlaceholder(jukebox);
            createJukebox(jukebox);
        }
    }
}
