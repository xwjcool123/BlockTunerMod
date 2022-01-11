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

package cool.xwj.blocktuner.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import cool.xwj.blocktuner.NoteNameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin extends DrawableHelper {

    private NoteNameHud noteNameHud;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void newNoteNameHud(MinecraftClient client, CallbackInfo ci) {
        noteNameHud = new NoteNameHud(client);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderNoteNameHud(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        this.noteNameHud.render(matrices);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
