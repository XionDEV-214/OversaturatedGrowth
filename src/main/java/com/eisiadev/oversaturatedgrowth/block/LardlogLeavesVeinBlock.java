package com.eisiadev.oversaturatedgrowth.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;

public class LardlogLeavesVeinBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 1, 2);

    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public LardlogLeavesVeinBlock(BlockBehaviour.Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 1).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(VARIANT, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        BlockState above = ctx.getLevel().getBlockState(pos.above());
        BlockState below = ctx.getLevel().getBlockState(pos.below());

        int variant = above.is(this) ? above.getValue(VARIANT) : 1 + ctx.getLevel().getRandom().nextInt(2);
        BlockState s = defaultBlockState().setValue(VARIANT, variant)
                .setValue(UP, above.is(this)).setValue(DOWN, below.is(this));
        return canSurvive(s, ctx.getLevel(), pos) ? s : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState up = level.getBlockState(above);
        return up.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock
                || up.getBlock() instanceof LardlogLeavesVeinBlock
                || up.isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                  net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (dir == Direction.UP && !canSurvive(state, level, pos)) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        if (dir == Direction.UP) return state.setValue(UP, neighbor.is(this));
        if (dir == Direction.DOWN) return state.setValue(DOWN, neighbor.is(this));
        return super.updateShape(state, dir, neighbor, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override public boolean isFlammable(BlockState s, BlockGetter l, BlockPos p, Direction f) { return true; }
    @Override public int getFlammability(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 15; }
    @Override public int getFireSpreadSpeed(BlockState s, BlockGetter l, BlockPos p, Direction f) { return 100; }
}
