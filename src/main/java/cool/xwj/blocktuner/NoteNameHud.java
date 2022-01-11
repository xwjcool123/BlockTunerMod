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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class NoteNameHud extends DrawableHelper {

    private final MinecraftClient client;
    private final TextRenderer textRenderer;

    public NoteNameHud(MinecraftClient client) {
        this.client = client;
        this.textRenderer = client.textRenderer;
    }

    public void render(MatrixStack matrices) {
        assert this.client.world != null;
        if(Screen.hasControlDown()) {
            HitResult hitResult = client.crosshairTarget;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState state = this.client.world.getBlockState(blockPos);
                if (state.getBlock() == Blocks.NOTE_BLOCK) {
                    int note = state.get(NoteBlock.NOTE);
                    int x = this.client.getWindow().getScaledWidth() / 2 + 4;
                    int y = this.client.getWindow().getScaledHeight() / 2 + 4;
                    this.textRenderer.drawWithShadow(matrices, BlockTunerClient.getNoteName(note), x, y, 0x55FFFF);
                }
            }
        }
    }
}
