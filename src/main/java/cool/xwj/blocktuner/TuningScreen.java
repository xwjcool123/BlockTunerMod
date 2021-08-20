package cool.xwj.blocktuner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TuningScreen extends HandledScreen<ScreenHandler> {

    private BlockPos pos;
    TuningScreenHandler screenHandler;

    private static final Identifier TEXTURE = new Identifier("blocktuner", "textures/gui/container/tune.png");

    public TuningScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (TuningScreenHandler) handler;
    }

    @Override
    protected void init() {
        super.init();

        // Fancy(?) keyboard

        this.addButton(new BlackKeyWidget(this.x+13, this.y+26, 0));    //
        this.addButton(new WhiteKeyWidget(this.x+18, this.y+43, 1));        //
        this.addButton(new BlackKeyWidget(this.x+23, this.y+26, 2));    //
        this.addButton(new WhiteKeyWidget(this.x+28, this.y+43, 3));        //
        this.addButton(new BlackKeyWidget(this.x+33, this.y+26, 4));    //
        this.addButton(new WhiteKeyWidget(this.x+38, this.y+43, 5));        //
        this.addButton(new WhiteKeyWidget(this.x+48, this.y+43, 6));        //
        this.addButton(new BlackKeyWidget(this.x+53, this.y+26, 7));    //
        this.addButton(new WhiteKeyWidget(this.x+58, this.y+43, 8));        //
        this.addButton(new BlackKeyWidget(this.x+63, this.y+26, 9));    //
        this.addButton(new WhiteKeyWidget(this.x+68, this.y+43, 10));       //
        this.addButton(new WhiteKeyWidget(this.x+78, this.y+43, 11));       //
        this.addButton(new BlackKeyWidget(this.x+83, this.y+26, 12));   //
        this.addButton(new WhiteKeyWidget(this.x+88, this.y+43, 13));       //
        this.addButton(new BlackKeyWidget(this.x+93, this.y+26, 14));   //
        this.addButton(new WhiteKeyWidget(this.x+98, this.y+43, 15));       //
        this.addButton(new BlackKeyWidget(this.x+103, this.y+26, 16));  //
        this.addButton(new WhiteKeyWidget(this.x+108, this.y+43, 17));      //
        this.addButton(new WhiteKeyWidget(this.x+118, this.y+43, 18));      //
        this.addButton(new BlackKeyWidget(this.x+123, this.y+26, 19));  //
        this.addButton(new WhiteKeyWidget(this.x+128, this.y+43, 20));      //
        this.addButton(new BlackKeyWidget(this.x+133, this.y+26, 21));  //
        this.addButton(new WhiteKeyWidget(this.x+138, this.y+43, 22));      //
        this.addButton(new WhiteKeyWidget(this.x+148, this.y+43, 23));      //
        this.addButton(new BlackKeyWidget(this.x+153, this.y+26, 24));  //

        pos = new BlockPos(client.player.raycast(5., 0.0f, false).getPos());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Environment(EnvType.CLIENT)
    abstract class PianoKeyWidget extends PressableWidget {
        protected int note;

        protected PianoKeyWidget(int x, int y, int note) {
            super(x, y, 9, 16, LiteralText.EMPTY);
            this.note = note;
        }

        public void onPress(){

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(note);
            buf.writeBlockPos(screenHandler.getSyncedPos());

            ClientPlayNetworking.send(BlockTuner.TUNE_PACKET, buf);
        }

    }

    class BlackKeyWidget extends PianoKeyWidget{
        public BlackKeyWidget(int x, int y, int note) {
            super(x, y, note);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (!this.active) {
                status += this.width * 1;
            } else if (this.isHovered()) {
                status += this.width * 2;
            }

            this.drawTexture(matrices, this.x, this.y, 176, 16 * status, this.width, this.height);
        }
    }

    class WhiteKeyWidget extends PianoKeyWidget{
        public WhiteKeyWidget(int x, int y, int note) {
            super(x, y, note);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (!this.active) {
                status += this.width * 1;
            } else if (this.isHovered()) {
                status += this.width * 2;
            }

            this.drawTexture(matrices, this.x, this.y, 192, 16 * status, this.width, this.height);
        }

    }

}
