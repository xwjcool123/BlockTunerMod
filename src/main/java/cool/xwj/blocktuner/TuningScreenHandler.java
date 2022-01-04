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

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;

public class TuningScreenHandler extends ScreenHandler {

    PropertyDelegate propertyDelegate;
    private final ScreenHandlerContext context;

    public TuningScreenHandler(int syncId){
        this(syncId, ScreenHandlerContext.EMPTY, new ArrayPropertyDelegate(3));
    }

    public TuningScreenHandler(int syncId, ScreenHandlerContext context, PropertyDelegate propertyDelegate){
        super(BlockTuner.TUNING_SCREEN_HANDLER, syncId);

        this.context = context;
        this.propertyDelegate = propertyDelegate;
        this.addProperties(propertyDelegate);
    }

    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Blocks.NOTE_BLOCK);
    }

    public BlockPos getSyncedPos() {
        return new BlockPos(propertyDelegate.get(0), propertyDelegate.get(1), propertyDelegate.get(2));
    }

}
