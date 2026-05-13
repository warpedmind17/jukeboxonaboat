package com.kasetoatz.jukeboxboat.mixin;

import com.kasetoatz.jukeboxboat.interfaces.IAbstractBoat;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;

import static com.kasetoatz.jukeboxboat.util.Util.*;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin implements IAbstractBoat
{
    @Shadow protected abstract boolean canAddPassenger(Entity passenger);

    @Unique
    private void setStoredDisc(ItemStack disc)
    {
        if ((Object)this instanceof Boat boat)
        {
            if (disc.isEmpty())
            {
                boat.setComponent(DataComponents.CUSTOM_DATA, boat.get(DataComponents.CUSTOM_DATA).update(nbtCompound -> nbtCompound.remove("stored_disc")));
            }
            else
            {
                boat.setComponent(DataComponents.CUSTOM_DATA, boat.get(DataComponents.CUSTOM_DATA).update(nbtCompound -> nbtCompound.store("stored_disc", ItemStack.CODEC, disc)));
            }
        }
    }

    @Unique
    private void setLastStateChange()
    {
        if ((Object)this instanceof Boat boat)
        {
            boat.setComponent(DataComponents.CUSTOM_DATA, boat.get(DataComponents.CUSTOM_DATA).update(nbtCompound -> nbtCompound.putLong("last_state_change", System.currentTimeMillis())));
        }
    }

    @Unique
    private boolean canExecute()
    {
        if ((Object)this instanceof Boat boat)
        {
            return System.currentTimeMillis() - boat.get(DataComponents.CUSTOM_DATA).copyTag().getLong("last_state_change").orElse(0L) > 100;
        }
        return true;
    }

    @Override
    public boolean jukeboxBoat$isJukeboxBoat()
    {
        if ((Object)this instanceof Boat boat)
        {
            return boat.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("is_jukebox_boat").orElse(false);
        }
        return false;
    }

    @Override
    public void jukeboxBoat$setJukeboxBoat(boolean value)
    {
        if ((Object)this instanceof Boat boat)
        {
            boat.setComponent(DataComponents.CUSTOM_DATA, boat.get(DataComponents.CUSTOM_DATA).update(nbtCompound -> nbtCompound.putBoolean("is_jukebox_boat", value)));
        }
    }

    @Override
    public ItemStack jukeboxBoat$getStoredDisc()
    {
        if ((Object)this instanceof Boat boat)
        {
            return boat.get(DataComponents.CUSTOM_DATA).copyTag().read("stored_disc", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public String jukeboxBoat$getJukebox()
    {
        if ((Object)this instanceof Boat boat)
        {
            return boat.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("jukebox", "");
        }
        return null;
    }

    @Override
    public void jukeboxBoat$setJukebox(String uuid)
    {
        if ((Object)this instanceof Boat boat)
        {
            boat.setComponent(DataComponents.CUSTOM_DATA, boat.get(DataComponents.CUSTOM_DATA).update(nbtCompound -> nbtCompound.putString("jukebox", uuid)));
        }
    }

    @ModifyReturnValue(method="interact", at=@At("RETURN"))
    public InteractionResult interact(InteractionResult result, @Local(argsOnly=true, name="player") Player player, @Local(argsOnly=true, name="hand") InteractionHand hand)
    {
        if ((Object)this instanceof Boat boat)
        {
            if (result != InteractionResult.PASS || !jukeboxBoat$isJukeboxBoat() || !canExecute())
            {
                return result;
            }
            else if (canAddPassenger(player) && !player.isSecondaryUseActive())
            {
                return InteractionResult.PASS;
            }
            else if (!jukeboxBoat$getStoredDisc().isEmpty())
            {
                ItemStack stack = jukeboxBoat$getStoredDisc();
                if (!stack.isEmpty())
                {
                    setPlaying(boat, false);
                    setStoredDisc(ItemStack.EMPTY);
                    player.getInventory().add(stack);
                }
                replaceJukebox(boat);
                setLastStateChange();
            }
            else
            {
                ItemStack stack = player.getItemInHand(hand);
                ItemStack storedDisc = stack.copy();
                SoundEvent sound = getDiscSound(storedDisc);
                if (sound == null)
                {
                    return InteractionResult.PASS;
                }
                stack.consumeAndReturn(1, player);
                Display.BlockDisplay jukebox = findJukebox(boat, jukeboxBoat$getJukebox());
                if (jukebox == null)
                {
                    jukebox = createJukebox(boat);
                }
                boat.level().getServer().getPlayerList().broadcastAll(new ClientboundSoundEntityPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                    SoundSource.RECORDS,
                    jukebox,
                    1.f,
                    1.f,
                    0L
                ));
                setPlaying(boat, true);
                setStoredDisc(storedDisc);
                setLastStateChange();
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Inject(method="tick", at=@At("HEAD"))
    public void tick(CallbackInfo ci)
    {
        if ((Object)this instanceof Boat boat)
        {
            IAbstractBoat accessor = (IAbstractBoat)boat;
            Display.BlockDisplay jukebox = findJukebox(boat, jukeboxBoat$getJukebox());
            if (!accessor.jukeboxBoat$isJukeboxBoat() || jukebox == null)
            {
                return;
            }
            if (boat.level() instanceof ServerLevel world)
            {
                jukebox.teleportTo(world, boat.getX(), boat.getY(), boat.getZ(), EnumSet.noneOf(Relative.class), boat.getYRot() + 90, boat.getXRot(), false);
            }
        }
    }
}
