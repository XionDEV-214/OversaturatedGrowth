package com.eisiadev.oversaturatedgrowth.event;

import com.eisiadev.oversaturatedgrowth.OversaturatedGrowth;
import com.eisiadev.oversaturatedgrowth.worldgen.castle.CastleLayout;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = OversaturatedGrowth.MODID)
public class CastleCommand {
    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("ogcastle").requires(s -> s.hasPermission(2))
                .executes(c -> run(c.getSource(), "full"))
                .then(Commands.argument("part", StringArgumentType.word())
                        .suggests((c, b) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                new String[]{"full", "keep", "tower", "maze"}, b))
                        .executes(c -> run(c.getSource(), StringArgumentType.getString(c, "part")))));
    }

    private static int run(CommandSourceStack src, String part) {
        ServerLevel level = src.getLevel();
        BlockPos p = BlockPos.containing(src.getPosition());
        long seed = level.getSeed() ^ p.asLong();
        CastleLayout.HeightFn hf = (hx, hz) -> level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, hx, hz) - 1;
        List<StructurePiece> pieces = "full".equals(part)
                ? CastleLayout.generate(p.getX(), hf.at(p.getX(), p.getZ()), p.getZ(), seed, hf, level.getMinBuildHeight(), level.getMaxBuildHeight())
                : CastleLayout.sample(part, p.getX(), p.getY(), p.getZ(), seed, hf, level.getMinBuildHeight(), level.getMaxBuildHeight());
        for (StructurePiece piece : pieces) {
            BoundingBox bb = piece.getBoundingBox();
            piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.random,
                    bb, new ChunkPos(bb.getCenter()), bb.getCenter());
        }
        src.sendSuccess(() -> Component.literal("Radical-Lardical Castle [" + part + "] placed: " + pieces.size() + " pieces"), true);
        return pieces.size();
    }
}
