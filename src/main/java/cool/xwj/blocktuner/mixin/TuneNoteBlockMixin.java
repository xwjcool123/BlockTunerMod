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

package cool.xwj.blocktuner.mixin;

import cool.xwj.blocktuner.BlockTuner;
import cool.xwj.blocktuner.TuningScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class TuneNoteBlockMixin extends Block {

    public TuneNoteBlockMixin(Settings settings) {
        super(settings);
    }

    // runs on server side while player trying to tune note blocks
    @Inject(method = "onUse",
            cancellable = true,
            at = @At(value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/block/BlockState;cycle(Lnet/minecraft/state/property/Property;)Ljava/lang/Object;"))

    private void onTune(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir){

        // allows playing with right clicks while holding blaze rods
        if (player.getMainHandStack().getItem() == Items.BLAZE_ROD) {

            if (world.getBlockState(pos.up()).isAir()) {
                world.addSyncedBlockEvent(pos, (NoteBlock) (Object) this, 0, 0);
            }

            cir.setReturnValue(ActionResult.CONSUME);

        }

        // opens tuning GUI (WIP)
//        if (player.getMainHandStack().getItem() == Items.BOOK) {
        if (BlockTuner.activeTuners.contains((ServerPlayerEntity) player)) {

            player.openHandledScreen(createScreenHandlerFactory(state, world, pos));

            cir.setReturnValue(ActionResult.CONSUME);

        }

    }

    @Environment(EnvType.CLIENT)
    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack((NoteBlock) (Object) this);
        int note = state.get(NoteBlock.NOTE);

        if (note != 0 && Screen.hasControlDown()) {
            NbtCompound tag = new NbtCompound();

            tag.putString("note", String.valueOf(note));
            stack.putSubTag("BlockStateTag", tag);

        }

        return stack;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (placer instanceof ServerPlayerEntity && BlockTuner.activeTuners.contains((ServerPlayerEntity) placer) && state.get(NoteBlock.NOTE) == 0) {
            ((PlayerEntity) placer).openHandledScreen(createScreenHandlerFactory(state, world, pos));
        }
    }

    // tuning UI

    private static final Text SCREEN_TITLE = new TranslatableText("container.tune");

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {

        ArrayPropertyDelegate propertyDelegate = new ArrayPropertyDelegate(3);
        propertyDelegate.set(0, pos.getX());
        propertyDelegate.set(1, pos.getY());
        propertyDelegate.set(2, pos.getZ());

        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> new TuningScreenHandler(i, playerInventory, propertyDelegate), SCREEN_TITLE);
    }

}

