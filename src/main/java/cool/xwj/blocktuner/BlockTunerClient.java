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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

@Environment(EnvType.CLIENT)
public class BlockTunerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockTunerConfig.load();
        MidiManager.getMidiManager().refreshMidiDevice();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (BlockTunerConfig.onBlockTunerServer
                    && Screen.hasControlDown()
                    && !player.isSpectator()
                    && !player.isSneaking()
                    && world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.NOTE_BLOCK
                    && player.getMainHandStack().getItem() != Items.BLAZE_ROD) {
                MinecraftClient client = MinecraftClient.getInstance();
                client.execute(() -> client.setScreen(new TuningScreen(Text.empty(), hitResult.getBlockPos())));
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // knowing a BlockTuner server
        ClientPlayNetworking.registerGlobalReceiver(BlockTuner.CLIENT_CHECK, (client, handler, buf, responseSender) -> {
            int serverProtocol = buf.readInt();
            if (BlockTuner.TUNING_PROTOCOL == serverProtocol) {
                MinecraftClient.getInstance().execute(() -> BlockTunerConfig.onBlockTunerServer = true);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> BlockTunerConfig.onBlockTunerServer = false);
    }
}
