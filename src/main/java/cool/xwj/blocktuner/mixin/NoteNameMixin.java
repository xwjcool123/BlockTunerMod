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

import cool.xwj.blocktuner.NoteNames;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class NoteNameMixin {

    private static final String BLOCK_STATE_KEY = "BlockStateTag";
    private static final String NOTE_KEY = "note";
    private static final Style NOTE_STYLE = Style.EMPTY.withColor(Formatting.AQUA);

    @Inject(method = "getName", at = @At("TAIL"), cancellable = true)
    private void getNoteName(CallbackInfoReturnable<Text> cir){
        if (((ItemStack)(Object)this).getItem() == Items.NOTE_BLOCK) {

            NbtCompound nbtCompound = ((ItemStack)(Object)this).getSubNbt(BLOCK_STATE_KEY);
            int note = 0;

            if (nbtCompound != null && nbtCompound.contains(NOTE_KEY, 3)) {
                note = nbtCompound.getInt(NOTE_KEY);
                if (note > 24 || note < 0) {
                    note = 0;
                }
            }
            cir.setReturnValue(MutableText.of(new TranslatableTextContent(((ItemStack)(Object)this).getTranslationKey(), null, null))
                    .append(MutableText.of(new LiteralTextContent(" (" + NoteNames.get(note) + ", "+ note + ")")).setStyle(NOTE_STYLE)));
        }
    }
}
