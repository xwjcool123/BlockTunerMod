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
    private static final String PLAY_MODE = "play-mode";
    private static final String KEY_TO_PIANO = "key-to-piano";
    private static final String MIDI_DEVICE = "midi-device";
    private static final String KEY_SIGNATURE = "key-signature";
    private static String midiDeviceName = "";
    private static boolean keyToPiano = false;
    private static boolean playMode = false;
    static int keySignature = 0;

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
            playMode = Boolean.parseBoolean(properties.getProperty(PLAY_MODE));
            keyToPiano = Boolean.parseBoolean(properties.getProperty(KEY_TO_PIANO));
            midiDeviceName = properties.getProperty(MIDI_DEVICE, "");
            try {
                keySignature = Integer.parseInt(properties.getProperty(KEY_SIGNATURE));
            } catch (NumberFormatException e) {
                keySignature = 0;
                properties.setProperty(KEY_SIGNATURE, keySignature + "");
                save();
            }
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

    public static boolean isPlayMode() {
        return playMode;
    }

    public static void togglePlayMode() {
        playMode = !playMode;
        properties.setProperty(PLAY_MODE, playMode + "");
    }

    public static boolean isKeyToPiano() {
        return keyToPiano;
    }

    public static void toggleKeyToPiano() {
        keyToPiano = !keyToPiano;
        properties.setProperty(KEY_TO_PIANO, keyToPiano + "");
    }

    public static int getKeySignature() {
        return keySignature;
    }

    public static void setKeySignature(int keySignature) {
        if (keySignature >= -7 && keySignature <= 7){
            BlockTunerConfig.keySignature = keySignature;
        } else {
            BlockTunerConfig.keySignature = 0;
        }
        properties.setProperty(KEY_SIGNATURE, BlockTunerConfig.keySignature + "");
    }

    public static void keyAddSharp() {
        if (keySignature < 7) {
            keySignature += 1;
        } else {
            keySignature = -7;
        }
        properties.setProperty(KEY_SIGNATURE, keySignature + "");
    }

    public static void keyAddFlat() {
        if (keySignature > -7) {
            keySignature -= 1;
        } else {
            keySignature = 7;
        }
        properties.setProperty(KEY_SIGNATURE, keySignature + "");
    }
}
