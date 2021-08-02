package cool.xwj.blocktuner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;


@Environment(EnvType.CLIENT)
public class BlockTunerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BlockTuner.TUNING_SCREEN_HANDLER, TuningScreen::new);

    }
}
