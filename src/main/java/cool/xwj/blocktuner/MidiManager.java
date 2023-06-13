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

import org.jetbrains.annotations.Nullable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.util.Objects;
import java.util.Vector;

public class MidiManager {
    private static final MidiManager midiManager = new MidiManager();
    public static final Vector<MidiDevice> transmitters = new Vector<MidiDevice>(0, 1);
    static int deviceIndex = 0;

    private MidiManager() {}

    public static MidiManager getMidiManager() {
        return midiManager;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public void loopDeviceIndex() {
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
    public MidiDevice getCurrentDevice() {
        return transmitters.get(deviceIndex);
    }

    public void refreshMidiDevice() {
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
            } catch (MidiUnavailableException ignored) {
            }
        }
    }
}