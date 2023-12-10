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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class NoteNameHud {

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.world != null;
        assert client.player != null;
        if(Screen.hasControlDown() && !client.player.isSpectator()) {
            HitResult hitResult = client.crosshairTarget;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState state = client.world.getBlockState(blockPos);
                if (state.getBlock() == Blocks.NOTE_BLOCK) {
                    int note = state.get(NoteBlock.NOTE);
                    int x = client.getWindow().getScaledWidth() / 2 + 4;
                    int y = client.getWindow().getScaledHeight() / 2 + 4;
                    context.drawText(client.textRenderer, NoteNames.get(note) + ", " + note, x, y, 0x55FFFF, true);
                }
            }
        }
    }
}
