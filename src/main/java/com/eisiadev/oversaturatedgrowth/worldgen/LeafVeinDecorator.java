package com.eisiadev.oversaturatedgrowth.worldgen;

import com.eisiadev.oversaturatedgrowth.init.ModBlocks;
import com.eisiadev.oversaturatedgrowth.init.ModTreeDecorators;
import com.eisiadev.oversaturatedgrowth.block.LardlogLeavesVeinBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class LeafVeinDecorator extends TreeDecorator {
    public static final Codec<LeafVeinDecorator> CODEC = Codec.floatRange(0.0f, 1.0f).fieldOf("probability")
            .xmap(LeafVeinDecorator::new, d -> d.probability).codec();

    private final float probability;

    public LeafVeinDecorator(float probability) { this.probability = probability; }

    @Override
    protected TreeDecoratorType<?> type() { return ModTreeDecorators.LEAF_VEIN.get(); }

    @Override
    public void place(Context ctx) {
        RandomSource r = ctx.random();
        for (BlockPos leaf : ctx.leaves()) {
            if (r.nextFloat() >= probability) continue;
            int want = 1 + r.nextInt(3);
            int len = 0;
            BlockPos p = leaf.below();
            while (len < want && ctx.isAir(p)) { len++; p = p.below(); }
            if (len == 0) continue;
            int variant = 1 + r.nextInt(2);
            p = leaf.below();
            for (int i = 0; i < len; i++) {
                BlockState st = ModBlocks.LARDLOG_LEAVES_VEIN.get().defaultBlockState()
                        .setValue(LardlogLeavesVeinBlock.VARIANT, variant)
                        .setValue(LardlogLeavesVeinBlock.UP, i > 0)
                        .setValue(LardlogLeavesVeinBlock.DOWN, i < len - 1);
                ctx.setBlock(p, st);
                p = p.below();
            }
        }
    }
}
