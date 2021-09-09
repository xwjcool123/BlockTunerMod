/*
 *     Copyright (c) 2021, xwjcool.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cool.xwj.blocktuner;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockTuner implements ModInitializer {

    public static final String MOD_ID = "blocktuner";
    public static final Identifier TUNE = new Identifier(MOD_ID, "tune_screen");
    public static final Identifier TUNE_PACKET = new Identifier(MOD_ID, "tune_packet");
    public static final ScreenHandlerType<TuningScreenHandler> TUNING_SCREEN_HANDLER;

    static {
        TUNING_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(TUNE, TuningScreenHandler::new);
    }

    @Override
    public void onInitialize() {
        System.out.println("Now Loading Block Tuner!");

        ServerPlayNetworking.registerGlobalReceiver(TUNE_PACKET, (server, player, handler, buf, responseSender) -> {

            int note = buf.readByte();
            BlockPos pos = buf.readBlockPos();
            World world = player.world;

            server.execute(() -> {
//                System.out.println("Tuned " + pos + " to " + note);

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
