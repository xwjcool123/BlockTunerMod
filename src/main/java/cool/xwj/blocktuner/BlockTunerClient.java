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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.*;
import java.util.Objects;
import java.util.Vector;


@Environment(EnvType.CLIENT)
public class BlockTunerClient implements ClientModInitializer {

    private static boolean keyToPiano = false;
    private static boolean playMode = false;

    // TODO: allow player to choose whether to use BlockTuner
    public static boolean tuningOn = true;

    public static final Vector<MidiDevice> transmitters = new Vector<>(0, 1);
    private static int deviceIndex = 0;

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BlockTuner.TUNING_SCREEN_HANDLER, TuningScreen::new);

        transmitters.add(null);

        // Handshake with a BlockTuner server
        ClientPlayNetworking.registerGlobalReceiver(BlockTuner.CLIENT_CHECK, (client, handler, buf, responseSender) -> {
            int serverProtocol = buf.readInt();
            if (BlockTuner.TUNING_PROTOCOL == serverProtocol) {
                ClientPlayNetworking.send(BlockTuner.CLIENT_CHECK, PacketByteBufs.empty());
            }
        });

    }

    public static boolean isPlayMode() {
        return playMode;
    }

    public static void togglePlayMode() {
        playMode = !playMode;
    }

    public static boolean isKeyToPiano() {
        return keyToPiano;
    }

    public static void toggleKeyToPiano() {
        keyToPiano = !keyToPiano;
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
        }
    }

    @Nullable
    public static MidiDevice getCurrentDevice(){
        return transmitters.get(deviceIndex);
    }

    private static void refreshMidiDevice(){
        MidiDevice device;

        transmitters.clear();

        transmitters.add(null);

        // Get a list of MIDI input device.

        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : infos) {

            try {
                device = MidiSystem.getMidiDevice(info);

                if (device.getMaxTransmitters() != 0 && !Objects.equals(info.getVendor(), "Oracle Corporation")) {
                    transmitters.add(device);
                }

            } catch (MidiUnavailableException ignored) {}

        }

    }

}
