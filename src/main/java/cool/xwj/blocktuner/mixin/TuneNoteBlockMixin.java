package cool.xwj.blocktuner.mixin;

import cool.xwj.blocktuner.TuningScreenHandler;
import cool.xwj.blocktuner.event.player.TuneNoteBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.*;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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

/*        ActionResult result = TuneNoteBlockCallback.EVENT.invoker().interact(state, world, pos, player, (NoteBlock) (Object) this);

        if (result != ActionResult.PASS){
            cir.setReturnValue(result);

        }*/

        // resets the note
        if (player.getMainHandStack().getItem() == Items.BARRIER) {

            world.setBlockState(pos, state.with(NOTE, 0), 3);
            world.addSyncedBlockEvent(pos, (NoteBlock) (Object) this, 0, 0);

            cir.setReturnValue(ActionResult.CONSUME);

        }

        // allows playing with right clicks while holding blaze rods
        if (player.getMainHandStack().getItem() == Items.BLAZE_ROD) {

            world.addSyncedBlockEvent(pos, (NoteBlock) (Object) this, 0, 0);

            cir.setReturnValue(ActionResult.CONSUME);

        }

        // opens tuning GUI (WIP)
        if (player.getMainHandStack().getItem() == Items.BOOK && world.getBlockState(pos.up()).isAir()) {

//            System.out.println(state.get(NOTE));

            player.openHandledScreen(createScreenHandlerFactory(state, world, pos));

            cir.setReturnValue(ActionResult.CONSUME);

        }

    }

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

