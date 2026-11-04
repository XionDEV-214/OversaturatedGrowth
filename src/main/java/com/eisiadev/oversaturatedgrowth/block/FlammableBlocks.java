package com.eisiadev.oversaturatedgrowth.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public final class FlammableBlocks {
    private FlammableBlocks() {}

    public static class Pillar extends RotatedPillarBlock {
        public Pillar(BlockBehaviour.Properties p) { super(p); }
        @Override public boolean isFlammable(BlockState s, BlockGetter l, BlockPos p, Direction f) { return true; }
        @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
        @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
    }

    public static class Plain extends Block {
        public Plain(BlockBehaviour.Properties p) { super(p); }
        @Override public boolean isFlammable(BlockState s, BlockGetter l, BlockPos p, Direction f) { return true; }
        @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
        @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 20; }
    }

    public static class Stairs extends StairBlock {
        public Stairs(Supplier<BlockState> b, BlockBehaviour.Properties p) { super(b, p); }
        @Override public boolean isFlammable(BlockState s, BlockGetter l, BlockPos p, Direction f) { return true; }
        @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
        @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 20; }
    }

    public static class Slab extends SlabBlock {
        public Slab(BlockBehaviour.Properties p) { super(p); }
        @Override public boolean isFlammable(BlockState s, BlockGetter l, BlockPos p, Direction f) { return true; }
        @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 5; }
        @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 20; }
    }

    public static class Leaves extends LeavesBlock {
        public Leaves(BlockBehaviour.Properties p) { super(p); }
        @Override public boolean isFlammable(BlockState s, BlockGetter l, BlockPos p, Direction f) { return true; }
        @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 30; }
        @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 60; }
    }
}
