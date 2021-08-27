package cool.xwj.blocktuner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import org.jetbrains.annotations.Nullable;

import javax.sound.midi.*;
import java.util.Vector;


@Environment(EnvType.CLIENT)
public class BlockTunerClient implements ClientModInitializer {

    private static boolean keyToPiano = false;
    private static boolean playMode = false;
    public static final Vector<MidiDevice> transmitters = new Vector<>(0, 1);
    private static int deviceIndex = 0;

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BlockTuner.TUNING_SCREEN_HANDLER, TuningScreen::new);

        MidiDevice device;
        transmitters.add(null);

        // Get a list of MIDI input devices.

        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : infos) {

            try {
                device = MidiSystem.getMidiDevice(info);

                if (device.getMaxTransmitters() != 0) {
                    transmitters.add(device);
                }

            } catch (MidiUnavailableException ignored) {}

        }

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

}
