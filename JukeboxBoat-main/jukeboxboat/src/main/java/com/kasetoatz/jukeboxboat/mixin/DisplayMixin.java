package com.kasetoatz.jukeboxboat.mixin;

import com.kasetoatz.jukeboxboat.interfaces.IDisplay;
import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.class)
public abstract class DisplayMixin implements IDisplay
{
    @Shadow
    protected abstract void setTransformation(Transformation transformation);

    @Override
    public void jukeboxBoat$setTransformation(Transformation transformation)
    {
        setTransformation(transformation);
    }
}
