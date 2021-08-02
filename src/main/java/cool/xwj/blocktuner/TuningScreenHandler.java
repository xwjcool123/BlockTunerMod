package cool.xwj.blocktuner;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class TuningScreenHandler extends ScreenHandler {
    public TuningScreenHandler(int syncId, PlayerInventory playerInventory){
        super(BlockTuner.TUNING_SCREEN_HANDLER, syncId);

        int l;
        for(l = 0; l < 3; ++l) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, 84 + l * 18));
            }
        }

        for(l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 142));
        }

    };

    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
