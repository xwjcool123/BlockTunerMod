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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.*;
import java.util.Objects;
import java.util.Vector;


@Environment(EnvType.CLIENT)
public class BlockTunerClient implements ClientModInitializer {

    public static final Vector<MidiDevice> transmitters = new Vector<>(0, 1);
    private static int deviceIndex = 0;

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BlockTuner.TUNING_SCREEN_HANDLER, TuningScreen::new);

        BlockTunerConfig.load();
        refreshMidiDevice();

        // Handshake with a BlockTuner server
        ClientPlayNetworking.registerGlobalReceiver(BlockTuner.CLIENT_CHECK, (client, handler, buf, responseSender) -> {
            int serverProtocol = buf.readInt();
            if (BlockTuner.TUNING_PROTOCOL == serverProtocol) {
                ClientPlayNetworking.send(BlockTuner.CLIENT_CHECK, PacketByteBufs.empty());
            }

        });

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

    public static String getNoteName(int note) {
        int keySignature = BlockTunerConfig.getKeySignature();
        String noteName = "";
        switch (note) {
            case 0:
            case 12:
            case 24:
                if (keySignature >= -1) {
                    noteName = "F\u266f";
                } else if (keySignature <= -3) {
                    noteName = "G\u266d";
                } else {
                    noteName = "F\u266f | G\u266d";
                }
                break;
            case 1:
            case 13:
                if (keySignature >= 6) {
                    noteName = "F\ud834\udd2a";
                } else if (keySignature <= 4 && keySignature >= -6) {
                    noteName = "G";
                    if (keySignature >= 3 || keySignature <= -5) {
                        noteName = "G\u266e";
                    }
                } else if (keySignature == 5){
                    noteName = "F\ud834\udd2a | G\u266e";
                } else {
                    noteName = "G\u266e | A\ud834\udd2b";
                }
                break;
            case 2:
            case 14:
                if (keySignature >= 1) {
                    noteName = "G\u266f";
                } else if (keySignature <= -1) {
                    noteName = "A\u266d";
                } else {
                    noteName = "G\u266f | A\u266d";
                }
                break;
            case 3:
            case 15:
                if (keySignature >= 7) {
                    noteName = "G\ud834\udd2a | A\u266e";
                } else if (keySignature >= -4) {
                    noteName = "A";
                    if (keySignature <= -3 || keySignature >= 5) {
                        noteName = "A\u266e";
                    }
                } else if (keySignature <= -6) {
                    noteName = "B\ud834\udd2b";
                } else {
                    noteName = "A\u266e | B\ud834\udd2b";
                }
                break;
            case 4:
            case 16:
                if (keySignature >= 3) {
                    noteName = "A\u266f";
                } else if (keySignature <= 1) {
                    noteName = "B\u266d";
                } else {
                    noteName = "A\u266f | B\u266d";
                }
                break;
            case 5:
            case 17:
                if (keySignature >= -2) {
                    noteName = "B";
                    if (keySignature <= -1 || keySignature >= 7) {
                        noteName = "B\u266e";
                    }
                } else if (keySignature <= -4) {
                    noteName = "C\u266d";
                } else {
                    noteName = "B\u266e | C\u266d";
                }
                break;
            case 6:
            case 18:
                if (keySignature >= 5) {
                    noteName = "B\u266f";
                } else if (keySignature <= 3) {
                    noteName = "C";
                    if (keySignature >= 2 || keySignature <= -6) {
                        noteName = "C\u266e";
                    }
                } else {
                    noteName = "B\u266f | C\u266e";
                }
                break;
            case 7:
            case 19:
                if (keySignature >= 0) {
                    noteName = "C\u266f";
                } else if (keySignature <= -2) {
                    noteName = "D\u266d";
                } else {
                    noteName = "C\u266f | D\u266d";
                }
                break;
            case 8:
            case 20:
                if (keySignature >= 7) {
                    noteName = "C\ud834\udd2a";
                } else if (keySignature >= -5 && keySignature <= 5) {
                    noteName = "D";
                    if (keySignature <= -4 || keySignature >= 4) {
                        noteName = "D\u266e";
                    }
                } else if (keySignature <= -7) {
                    noteName = "E\ud834\udd2b";
                } else if (keySignature == -6) {
                    noteName = "C\ud834\udd2a | D\u266e";
                } else {
                    noteName = "D\u266e | E\ud834\udd2b";
                }
                break;
            case 9:
            case 21:
                if (keySignature >= 2) {
                    noteName = "D\u266f";
                } else if (keySignature <= 0) {
                    noteName = "E\u266d";
                } else {
                    noteName = "D\u266f | E\u266d";
                }
                break;
            case 10:
            case 22:
                if (keySignature >= -3) {
                    noteName = "E";
                    if (keySignature <= -2 || keySignature >= 6) {
                        noteName = "E\u266e";
                    }
                } else if (keySignature <= -5) {
                    noteName = "F\u266d";
                } else {
                    noteName = "E\u266e | F\u266d";
                }
                break;
            case 11:
            case 23:
                if (keySignature >= 4) {
                    noteName = "E\u266f";
                } else if (keySignature <= 2) {
                    noteName = "F";
                    if (keySignature >= 1 || keySignature <= -7) {
                        noteName = "F\u266e";
                    }
                } else {
                    noteName = "E\u266f | F\u266e";
                }
            }
        return noteName;
    }
}
