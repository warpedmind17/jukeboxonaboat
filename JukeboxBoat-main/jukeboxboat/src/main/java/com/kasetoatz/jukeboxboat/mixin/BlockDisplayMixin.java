package com.kasetoatz.jukeboxboat.mixin;

import com.kasetoatz.jukeboxboat.interfaces.IBlockDisplay;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.BlockDisplay.class)
public abstract class BlockDisplayMixin implements IBlockDisplay
{
    @Shadow
    protected abstract void setBlockState(BlockState blockState);

    @Override
    public String jukeboxBoat$getUUID()
    {
        CustomData nbt = ((Entity)(Object)this).get(DataComponents.CUSTOM_DATA);
        if (nbt != null)
        {
            return nbt.copyTag().getString("uuid").orElse("");
        }
        return "";
    }

    @Override
    public void jukeboxBoat$setUUID(String uuid)
    {
        Entity entity = ((Entity)(Object)this);
        CustomData nbt = entity.get(DataComponents.CUSTOM_DATA);
        if (nbt != null)
        {
            nbt.update(nbtCompound -> nbtCompound.putString("uuid", uuid));
            entity.setComponent(DataComponents.CUSTOM_DATA, nbt);
        }
    }

    @Override
    public void jukeboxBoat$setBlockState(BlockState state)
    {
        setBlockState(state);
    }
}
