package cool.xwj.blocktuner;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

//import static net.fabricmc.fabric.impl.networking.NetworkingImpl.MOD_ID;

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

            server.execute(() -> {
//                System.out.println("Tuned " + pos + " to " + note);
                if (player.world.getBlockState(pos).getBlock() == Blocks.NOTE_BLOCK) {
                    player.world.setBlockState(pos, player.world.getBlockState(pos).with(NoteBlock.NOTE, note), 3);
                    if (player.world.getBlockState(pos.up()).isAir()) {
                        player.world.addSyncedBlockEvent(pos, Blocks.NOTE_BLOCK, 0, 0);
                    }
                }
            });
        });

    }

}
