package cool.xwj.blocktuner.mixin;

import cool.xwj.blocktuner.TuningScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.*;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class TuneNoteBlockMixin{

    @Shadow @Final public static IntProperty NOTE;

    // runs on server side while player trying to tune note blocks
    @Inject(method = "onUse",
            cancellable = true,
            at = @At(value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/block/BlockState;cycle(Lnet/minecraft/state/property/Property;)Ljava/lang/Object;"))

    private void onTune(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir){

        // resets the note
        if (player.getMainHandStack().getItem() == Items.BARRIER) {

            world.setBlockState(pos, state.with(NOTE, 0), 3);
            if (world.getBlockState(pos.up()).isAir()) {
                world.addSyncedBlockEvent(pos, (NoteBlock) (Object) this, 0, 0);
            }
            cir.setReturnValue(ActionResult.CONSUME);

        }

        // allows playing with right clicks while holding blaze rods
        if (player.getMainHandStack().getItem() == Items.BLAZE_ROD) {

            if (world.getBlockState(pos.up()).isAir()) {
                world.addSyncedBlockEvent(pos, (NoteBlock) (Object) this, 0, 0);
            }

            cir.setReturnValue(ActionResult.CONSUME);

        }

        // opens tuning GUI (WIP)
        if (player.getMainHandStack().getItem() == Items.BOOK) {

//            System.out.println(state.get(NOTE));

            player.openHandledScreen(createScreenHandlerFactory(state, world, pos));

            cir.setReturnValue(ActionResult.CONSUME);

        }

    }

    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack((NoteBlock) (Object) this);
        int note = state.get(((NoteBlock) (Object) this).NOTE);

        if (note != 0) {
            NbtCompound tag = new NbtCompound();

            tag.putString("note", String.valueOf(note));
            stack.putSubTag("BlockStateTag", tag);

        }

        return stack;
    }

    // tuning UI

    private static final Text SCREEN_TITLE = new TranslatableText("container.tune");

    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {

        ArrayPropertyDelegate propertyDelegate = new ArrayPropertyDelegate(3);
        propertyDelegate.set(0, pos.getX());
        propertyDelegate.set(1, pos.getY());
        propertyDelegate.set(2, pos.getZ());

        return new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) -> {

            return new TuningScreenHandler(i, playerInventory, propertyDelegate);

        }, SCREEN_TITLE);
    }

}

