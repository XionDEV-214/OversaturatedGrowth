package com.eisiadev.oversaturatedgrowth.event.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

public class EntityConsumedEvent extends Event {

    private final ServerPlayer predator;
    private final LivingEntity prey;

    private final int calories;

    public EntityConsumedEvent(ServerPlayer predator, LivingEntity prey, int calories) {
        this.predator = predator;
        this.prey     = prey;
        this.calories = calories;
    }

    public ServerPlayer getPredator() { return predator; }
    public LivingEntity getPrey()     { return prey; }
    public int getCalories()          { return calories; }
}
