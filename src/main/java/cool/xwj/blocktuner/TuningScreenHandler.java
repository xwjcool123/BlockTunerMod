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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class TuningScreenHandler extends ScreenHandler {

    PropertyDelegate propertyDelegate;
    private BlockPos syncedPos = null;

    public TuningScreenHandler(int syncId, PlayerInventory playerInventory){
        this(syncId, playerInventory, new ArrayPropertyDelegate(3));
    }

    public TuningScreenHandler(int syncId, PlayerInventory playerInventory, PropertyDelegate propertyDelegate){
        super(BlockTuner.TUNING_SCREEN_HANDLER, syncId);

        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);

        int l;
        for(l = 0; l < 3; ++l) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, 84 + l * 18));
            }
        }

        for(l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 142));
        }

    }

    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public BlockPos getSyncedPos() {
        if (syncedPos == null){
            syncedPos = new BlockPos(propertyDelegate.get(0), propertyDelegate.get(1), propertyDelegate.get(2));
        }
        return syncedPos;
    }
}
