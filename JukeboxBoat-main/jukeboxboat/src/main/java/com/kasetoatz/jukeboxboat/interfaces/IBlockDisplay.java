package com.kasetoatz.jukeboxboat.interfaces;

import net.minecraft.world.level.block.state.BlockState;

public interface IBlockDisplay
{
    String jukeboxBoat$getUUID();
    void jukeboxBoat$setUUID(String uuid);
    void jukeboxBoat$setBlockState(BlockState state);
}
