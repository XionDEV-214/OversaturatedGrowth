package com.eisiadev.oversaturatedgrowth.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class ReappearingBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty VANISHED = BooleanProperty.create("vanished");
    private static final VoxelShape VANISHED_SHAPE = box(6, 6, 6, 10, 10, 10);

    public ReappearingBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(VANISHED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE, VANISHED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return state.getValue(VANISHED) ? VANISHED_SHAPE : super.getShape(state, getter, pos, ctx);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return state.getValue(VANISHED) ? Shapes.empty() : super.getCollisionShape(state, getter, pos, ctx);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!state.getValue(VANISHED) && !state.getValue(ACTIVE)) {
            activate(level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter getter, BlockPos pos, Explosion explosion) {
        return !state.getValue(ACTIVE) ? 6000F : super.getExplosionResistance(state, getter, pos, explosion);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter getter, BlockPos pos, Entity entity) {
        return state.getValue(ACTIVE) && super.canEntityDestroy(state, getter, pos, entity);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide()) return;
        if (!state.getValue(VANISHED) && !state.getValue(ACTIVE) && level.hasNeighborSignal(pos)) activate(level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(VANISHED)) {
            if (state.getValue(ACTIVE)) {
                level.setBlockAndUpdate(pos, state.setValue(VANISHED, false).setValue(ACTIVE, false));
            } else {
                level.setBlockAndUpdate(pos, state.setValue(ACTIVE, true));
                level.scheduleTick(pos, this, 15);
            }
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.3F, 0.6F);
        } else if (state.getValue(ACTIVE)) {
            level.setBlockAndUpdate(pos, state.setValue(ACTIVE, false).setValue(VANISHED, true));
            level.scheduleTick(pos, this, 80);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 0.5F);
            for (Direction d : Direction.values()) activate(level, pos.relative(d));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) sparkle(level, pos);
    }

    private static void sparkle(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        double d0 = 0.0625D;
        for (int i = 0; i < 6; ++i) {
            double d1 = pos.getX() + random.nextFloat();
            double d2 = pos.getY() + random.nextFloat();
            double d3 = pos.getZ() + random.nextFloat();
            if (i == 0 && !level.getBlockState(pos.above()).isSolidRender(level, pos)) d2 = pos.getY() + d0 + 1.0D;
            if (i == 1 && !level.getBlockState(pos.below()).isSolidRender(level, pos)) d2 = pos.getY() - d0;
            if (i == 2 && !level.getBlockState(pos.south()).isSolidRender(level, pos)) d3 = pos.getZ() + d0 + 1.0D;
            if (i == 3 && !level.getBlockState(pos.north()).isSolidRender(level, pos)) d3 = pos.getZ() - d0;
            if (i == 4 && !level.getBlockState(pos.east()).isSolidRender(level, pos)) d1 = pos.getX() + d0 + 1.0D;
            if (i == 5 && !level.getBlockState(pos.west()).isSolidRender(level, pos)) d1 = pos.getX() - d0;
            if (d1 < pos.getX() || d1 > pos.getX() + 1 || d2 < 0.0D || d2 > pos.getY() + 1 || d3 < pos.getZ() || d3 > pos.getZ() + 1) {
                level.addParticle(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F), d1, d2, d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void activate(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ReappearingBlock && !state.getValue(VANISHED) && !state.getValue(ACTIVE)) {
            level.setBlockAndUpdate(pos, state.setValue(ACTIVE, true));
            level.scheduleTick(pos, state.getBlock(), 2 + level.getRandom().nextInt(5));
        }
    }
}
