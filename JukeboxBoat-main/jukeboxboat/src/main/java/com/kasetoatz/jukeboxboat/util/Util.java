package com.kasetoatz.jukeboxboat.util;

import com.kasetoatz.jukeboxboat.interfaces.IAbstractBoat;
import com.kasetoatz.jukeboxboat.interfaces.IArmorStand;
import com.kasetoatz.jukeboxboat.interfaces.IBlockDisplay;
import com.kasetoatz.jukeboxboat.interfaces.IDisplay;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;

public class Util
{
    public static SoundEvent getDiscSound(ItemStack disc)
    {
        JukeboxPlayable component = disc.get(DataComponents.JUKEBOX_PLAYABLE);
        if (component == null)
        {
            return null;
        }
        return component.song().value().soundEvent().value();
    }

    public static void setPlaying(Boat boat, boolean playing)
    {
        Display.BlockDisplay jukebox = findJukebox(boat, ((IAbstractBoat)boat).jukeboxBoat$getJukebox());
        if (jukebox != null)
        {
            IBlockDisplay accessor = (IBlockDisplay)jukebox;
            if (playing)
            {
                accessor.jukeboxBoat$setBlockState(Blocks.JUKEBOX.defaultBlockState().setValue(JukeboxBlock.HAS_RECORD, true));
            }
            else
            {
                accessor.jukeboxBoat$setBlockState(Blocks.JUKEBOX.defaultBlockState());
            }
        }
    }

    public static Display.BlockDisplay createJukebox(Boat boat)
    {
        Display.BlockDisplay jukebox = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, boat.level());
        IBlockDisplay accessor = (IBlockDisplay)jukebox;
        accessor.jukeboxBoat$setBlockState(Blocks.JUKEBOX.defaultBlockState());
        //noinspection DataFlowIssue
        ((IDisplay)jukebox).jukeboxBoat$setTransformation(new Transformation(
                new Vector3f(-0.8f, 0.2f, -0.4f),
                new Quaternionf(),
                new Vector3f(0.8f, 0.8f, 0.8f),
                new Quaternionf()
        ));
        boat.level().addFreshEntity(jukebox);
        accessor.jukeboxBoat$setUUID(jukebox.getStringUUID());
        ((IAbstractBoat)boat).jukeboxBoat$setJukebox(jukebox.getStringUUID());
        return jukebox;
    }

    public static void spawnPlaceholder(Boat boat)
    {
        ArmorStand placeholder = new ArmorStand(boat.level(), boat.getX(), boat.getY(), boat.getZ());
        IArmorStand accessor = (IArmorStand)placeholder;
        accessor.jukeboxBoat$setPlaceholder(true);
        accessor.jukeboxBoat$setMarker(true);
        placeholder.setNoBasePlate(true);
        placeholder.setInvisible(true);
        placeholder.setInvulnerable(true);
        boat.level().addFreshEntity(placeholder);
        placeholder.startRiding(boat);
    }

    public static void removePlaceholder(Boat boat)
    {
        for (Entity entity : boat.getPassengers())
        {
            if (entity instanceof ArmorStand armorStand && ((IArmorStand)armorStand).jukeboxBoat$isPlaceholder())
            {
                armorStand.discard();
            }
        }
    }

    public static void replaceJukebox(Boat boat)
    {
        Display.BlockDisplay old = findJukebox(boat, ((IAbstractBoat)boat).jukeboxBoat$getJukebox());
        if (old != null)
        {
            old.discard();
        }
        createJukebox(boat);
    }

    private static Display.BlockDisplay iterateJukeboxes(Boat boat, String uuid)
    {
        if (boat.level() instanceof ServerLevel world)
        {
            for (Entity entity : world.getAllEntities())
            {
                if (entity instanceof Display.BlockDisplay jukebox)
                {
                    IBlockDisplay accessor = (IBlockDisplay)jukebox;
                    if (accessor.jukeboxBoat$getUUID().equals(uuid))
                    {
                        ((IAbstractBoat)accessor).jukeboxBoat$setJukebox(jukebox.getStringUUID());
                        accessor.jukeboxBoat$setUUID(jukebox.getStringUUID());
                        return jukebox;
                    }
                }
            }
        }
        return null;
    }

    public static Display.BlockDisplay findJukebox(Boat boat, String uuid)
    {
        if (uuid.isEmpty())
        {
            return null;
        }
        Entity possible = boat.level().getEntity(UUID.fromString(uuid));
        if (possible instanceof Display.BlockDisplay jukebox)
        {
            return jukebox;
        }
        return iterateJukeboxes(boat, uuid);
    }
}
