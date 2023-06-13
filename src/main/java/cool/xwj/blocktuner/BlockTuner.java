/*
 *     Copyright (c) 2021, xwjcool.
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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockTuner implements ModInitializer {

    public static final String MOD_ID = "blocktuner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier CLIENT_CHECK = identifier("client_check");
    public static final Identifier TUNING_CHANNEL = identifier("tune");

    // tuning protocol version. The number on client needs to match that on server.
    public static final int TUNING_PROTOCOL = 2;

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[BlockTuner] Now Loading BlockTuner!");
        CommandRegistrationCallback.EVENT.register(BlockTunerCommands::register);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(BlockTuner.TUNING_PROTOCOL);
            sender.sendPacket(BlockTuner.CLIENT_CHECK, buf);
        });

        ServerPlayNetworking.registerGlobalReceiver(TUNING_CHANNEL, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int note = buf.readByte();
            World world = player.getWorld();
            if (world.getBlockState(pos).getBlock() != Blocks.NOTE_BLOCK) {
                return;
            }
            server.execute(() -> {
                world.setBlockState(pos, world.getBlockState(pos).with(NoteBlock.NOTE, note), 3);
                if (world.getBlockState(pos.up()).isAir()) {
                    ((NoteBlock) world.getBlockState(pos).getBlock()).onSyncedBlockEvent(world.getBlockState(pos), world, pos, 0, 0);
                    ((ServerWorld)world).spawnParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 0, (double)note / 24.0D, 0.0D, 0.0D, 1.0D);
                }
                player.swingHand(Hand.MAIN_HAND);
            });
        });
    }
}
