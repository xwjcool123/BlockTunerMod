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
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

import static net.minecraft.server.command.CommandManager.literal;

public class BlockTuner implements ModInitializer {

    public static final String MOD_ID = "blocktuner";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // tuning protocol version. The number on client needs to match that on server.
    public static final int TUNING_PROTOCOL = 0;

    public static final HashSet<ServerPlayerEntity> validTuners = new HashSet<>();
    public static final HashSet<ServerPlayerEntity> activeTuners = new HashSet<>();

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }
    public static final Identifier TUNING_SCREEN = identifier("tune_screen");
    public static final Identifier TUNE_PACKET = identifier("tune_packet");
    public static final Identifier CLIENT_CHECK = identifier("client_check");

    public static final ScreenHandlerType<TuningScreenHandler> TUNING_SCREEN_HANDLER;

    static {
        TUNING_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(TUNING_SCREEN, (syncId, playerInventory) -> new TuningScreenHandler(syncId));
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[BlockTuner] Now Loading Block Tuner!");

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(literal("blocktuner").requires(source ->
                source.getEntity() instanceof ServerPlayerEntity && validTuners.contains((ServerPlayerEntity) source.getEntity())).executes(context -> {

            ServerCommandSource source = context.getSource();
            ServerPlayerEntity player = source.getPlayer();

            if (!activeTuners.contains(player)) {

                activeTuners.add(player);
                source.sendFeedback(new TranslatableText("blocktuner.enable"), false);
                return 1;

            } else {

                activeTuners.remove(player);
                source.sendFeedback(new TranslatableText("blocktuner.disable"), false);
                return 0;

            }

        })));

        ServerPlayNetworking.registerGlobalReceiver(CLIENT_CHECK, (server, player, handler, buf, responseSender) -> {

            validTuners.add(player);
            activeTuners.add(player);
            server.execute(() -> {
                server.getCommandManager().sendCommandTree(player);
                player.sendSystemMessage(new TranslatableText("blocktuner.available"), Util.NIL_UUID);
            });

        });

        ServerPlayNetworking.registerGlobalReceiver(TUNE_PACKET, (server, player, handler, buf, responseSender) -> {

            int note = buf.readByte();
            BlockPos pos = buf.readBlockPos();
            World world = player.world;

            server.execute(() -> {

                if (world.getBlockState(pos).getBlock() == Blocks.NOTE_BLOCK) {

                    world.setBlockState(pos, world.getBlockState(pos).with(NoteBlock.NOTE, note), 3);

                    if (world.getBlockState(pos.up()).isAir()) {

//                        world.addSyncedBlockEvent(pos, Blocks.NOTE_BLOCK, 0, 0);
                        ((NoteBlock) world.getBlockState(pos).getBlock()).onSyncedBlockEvent(world.getBlockState(pos), world, pos, 0, 0);
                        ((ServerWorld)world).spawnParticles(ParticleTypes.NOTE, pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 0, (double)note / 24.0D, 0.0D, 0.0D, 1.0D);

                    }

                    player.swingHand(Hand.MAIN_HAND);
                }

            });

        });

    }

}
