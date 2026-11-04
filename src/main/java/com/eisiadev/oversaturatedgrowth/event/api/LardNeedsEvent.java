package com.eisiadev.oversaturatedgrowth.event.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class LardNeedsEvent extends Event {

    private final ServerPlayer player;
    private final int calories;

    public LardNeedsEvent(ServerPlayer player, int calories) {
        this.player   = player;
        this.calories = calories;
    }

    public ServerPlayer getPlayer()  { return player; }
    public int getCalories()         { return calories; }
}
