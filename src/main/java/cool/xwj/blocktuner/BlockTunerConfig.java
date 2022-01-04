/*
 *     Copyright (c) 2022, xwjcool.
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

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class BlockTunerConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("blocktuner.properties");
    private static final Properties properties = new Properties();

    // configurables
    private static final String MIDI_DEVICE = "midi-device";
    private static String midiDeviceName = "";

    public static void save(){

        if (!Files.exists(CONFIG_PATH)) {
            try {
                Files.createFile(CONFIG_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(outputStream, "BlockTuner Configuration File");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void load(){

        if (!Files.exists(CONFIG_PATH)) {
            try {
                Files.createFile(CONFIG_PATH);
                save();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            properties.load(inputStream);

            //
            midiDeviceName = properties.getProperty(MIDI_DEVICE, "");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getMidiDeviceName() {
        return midiDeviceName;
    }

    public static void setMidiDeviceName(String midiDeviceName) {
        BlockTunerConfig.midiDeviceName = midiDeviceName;
        properties.setProperty(MIDI_DEVICE, midiDeviceName);
    }

}
