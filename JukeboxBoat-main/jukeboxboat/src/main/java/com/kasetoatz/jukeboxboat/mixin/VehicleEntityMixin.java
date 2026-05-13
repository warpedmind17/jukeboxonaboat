package com.kasetoatz.jukeboxboat.mixin;

import com.kasetoatz.jukeboxboat.interfaces.IAbstractBoat;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.kasetoatz.jukeboxboat.util.Util.findJukebox;
import static com.kasetoatz.jukeboxboat.util.Util.removePlaceholder;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin
{
    @Unique
    private void delete()
    {
        if ((Object)this instanceof Boat boat)
        {
            IAbstractBoat accessor = (IAbstractBoat)boat;
            Display.BlockDisplay jukebox = findJukebox(boat, accessor.jukeboxBoat$getJukebox());
            if (jukebox != null)
            {
                jukebox.discard();
                accessor.jukeboxBoat$setJukebox("");
            }
            removePlaceholder(boat);
        }
    }

    @Inject(method="hurtServer", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/vehicle/VehicleEntity;discard()V"))
    public void discard(CallbackInfoReturnable<Boolean> cir)
    {
        delete();
    }

    @Inject(method="hurtServer", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/vehicle/VehicleEntity;destroy(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    public void preKill(CallbackInfoReturnable<Boolean> cir)
    {
        delete();
    }

    @Inject(method="destroy(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/Item;)V", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/vehicle/VehicleEntity;spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public void killAndDropItem(CallbackInfo ci, @Local(argsOnly=true, name="level") ServerLevel level, @Local(name="itemStack") ItemStack itemStack)
    {
        if ((Object)this instanceof Boat boat)
        {
            IAbstractBoat accessor = (IAbstractBoat)boat;
            ItemStack disc = accessor.jukeboxBoat$getStoredDisc();
            if (!disc.isEmpty())
            {
                boat.spawnAtLocation(level, disc);
            }
            itemStack.set(DataComponents.ITEM_NAME, Component.translatable("jukeboxboat.item.name"));
        }
    }
}
