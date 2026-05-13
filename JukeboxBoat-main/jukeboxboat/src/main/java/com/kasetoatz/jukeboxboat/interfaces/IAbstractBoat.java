package com.kasetoatz.jukeboxboat.interfaces;

import net.minecraft.world.item.ItemStack;

public interface IAbstractBoat
{
    boolean jukeboxBoat$isJukeboxBoat();
    void jukeboxBoat$setJukeboxBoat(boolean value);
    ItemStack jukeboxBoat$getStoredDisc();
    String jukeboxBoat$getJukebox();
    void jukeboxBoat$setJukebox(String uuid);
}
