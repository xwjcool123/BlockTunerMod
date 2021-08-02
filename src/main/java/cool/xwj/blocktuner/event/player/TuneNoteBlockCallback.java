package cool.xwj.blocktuner.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface TuneNoteBlockCallback {

    Event<TuneNoteBlockCallback> EVENT = EventFactory.createArrayBacked(TuneNoteBlockCallback.class, (listeners) -> (state, world, pos, player, noteBlock) -> {
        for (TuneNoteBlockCallback listener : listeners) {
            ActionResult result = listener.interact(state, world, pos, player, noteBlock);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player, NoteBlock noteBlock);
}
