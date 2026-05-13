package com.kasetoatz.jukeboxboat.mixin;

import com.kasetoatz.jukeboxboat.interfaces.IArmorStand;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin implements IArmorStand
{
    @Shadow protected abstract void setMarker(boolean value);

    @Override
    public boolean jukeboxBoat$isPlaceholder()
    {
        if ((Object)this instanceof ArmorStand entity)
        {
            return entity.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("is_placeholder").orElse(false);
        }
        return false;
    }

    @Override
    public void jukeboxBoat$setPlaceholder(boolean value)
    {
        if ((Object)this instanceof ArmorStand entity)
        {
            entity.setComponent(DataComponents.CUSTOM_DATA, entity.get(DataComponents.CUSTOM_DATA).update(nbtCompound -> nbtCompound.putBoolean("is_placeholder", value)));
        }
    }

    @Override
    public void jukeboxBoat$setMarker(boolean value)
    {
        setMarker(value);
    }
}