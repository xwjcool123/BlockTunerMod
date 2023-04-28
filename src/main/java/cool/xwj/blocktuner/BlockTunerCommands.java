/*
 *     Copyright (c) 2023, xwjcool.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cool.xwj.blocktuner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

public class BlockTunerCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("tune")
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("note", IntegerArgumentType.integer(0, 24))
                .executes(context -> tune(context.getSource(), BlockPosArgumentType.getLoadedBlockPos(context, "pos"), IntegerArgumentType.getInteger(context, "note"))))
        ));
    }

    private static int tune(ServerCommandSource source, BlockPos pos, int note){
        ServerWorld world = source.getWorld();
        if (world.getBlockState(pos).getBlock() != Blocks.NOTE_BLOCK || (!source.hasPermissionLevel(2) && pos.isWithinDistance(source.getPosition(), 5.0d))) {
            return -1;
        }
        world.setBlockState(pos, world.getBlockState(pos).with(NoteBlock.NOTE, note));
        // please do not change this to world.addSyncedBlockEvent() as it does not allow chords to be played.
        world.getBlockState(pos).onSyncedBlockEvent(world, pos, 0, 0);
        world.spawnParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 0, (double)note / 24.0D, 0.0D, 0.0D, 1.0D);
        world.emitGameEvent(source.getEntity(), GameEvent.NOTE_BLOCK_PLAY, pos);
        return note;
    }
}
