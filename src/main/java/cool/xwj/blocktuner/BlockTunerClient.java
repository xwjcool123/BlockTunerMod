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
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.*;
import java.util.Objects;
import java.util.Vector;


@Environment(EnvType.CLIENT)
public class BlockTunerClient implements ClientModInitializer {

    public static final Vector<MidiDevice> transmitters = new Vector<>(0, 1);
    private static int deviceIndex = 0;
    private boolean blockTunerServer = false;

    @Override
    public void onInitializeClient() {
        BlockTunerConfig.load();
        refreshMidiDevice();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if(blockTunerServer
                    && !player.isSneaking()
                    && world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.NOTE_BLOCK
                    && !player.isSpectator()
                    && player.getMainHandStack().getItem() != Items.BLAZE_ROD) {
                MinecraftClient.getInstance().setScreen(new TuningScreen(Text.empty(), hitResult.getBlockPos()));
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        // knowing a BlockTuner server
        ClientPlayNetworking.registerGlobalReceiver(BlockTuner.CLIENT_CHECK, (client, handler, buf, responseSender) -> {
            int serverProtocol = buf.readInt();
            if (BlockTuner.TUNING_PROTOCOL == serverProtocol) {
                MinecraftClient.getInstance().execute(() -> blockTunerServer = true);
            }

        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> blockTunerServer = false);

    }

    public static int getDeviceIndex() {
        return deviceIndex;
    }

    public static void loopDeviceIndex() {
        if (deviceIndex == 0) {
            refreshMidiDevice();
        }
        if (deviceIndex < transmitters.size() - 1) {
            deviceIndex += 1;
        } else {
            deviceIndex = 0;
            BlockTunerConfig.setMidiDeviceName("");
        }

    }

    @Nullable
    public static MidiDevice getCurrentDevice(){
        return transmitters.get(deviceIndex);
    }

    public static void refreshMidiDevice() {
        MidiDevice device;
        transmitters.clear();
        transmitters.add(null);
        deviceIndex = 0;

        // Get a list of MIDI input device.
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {

            try {
                device = MidiSystem.getMidiDevice(info);

                if (device.getMaxTransmitters() != 0 && !Objects.equals(info.getVendor(), "Oracle Corporation")) {

                    transmitters.add(device);

                    if (info.getName().equals(BlockTunerConfig.getMidiDeviceName())) {
                        deviceIndex = transmitters.size() - 1;
                    }

                }

            } catch (MidiUnavailableException ignored) {}

        }

    }

}
