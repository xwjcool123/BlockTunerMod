package cool.xwj.blocktuner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TuningScreen extends HandledScreen<ScreenHandler> {

    TuningScreenHandler screenHandler;
    private BlockPos pos = null;
    private PianoKeyWidget pressedKey = null;

    private static final Identifier TEXTURE = new Identifier("blocktuner", "textures/gui/container/tune.png");

    public TuningScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (TuningScreenHandler) handler;
    }

    @Override
    protected void init() {
        super.init();

        // Fancy(?) keyboard

        this.addButton(new WhiteKeyWidget(this.x+18, this.y+44, 1, 3));
        this.addButton(new WhiteKeyWidget(this.x+28, this.y+44, 3, 4));
        this.addButton(new WhiteKeyWidget(this.x+38, this.y+44, 5, 5));
        this.addButton(new WhiteKeyWidget(this.x+48, this.y+44, 6, 1));
        this.addButton(new WhiteKeyWidget(this.x+58, this.y+44, 8, 2));
        this.addButton(new WhiteKeyWidget(this.x+68, this.y+44, 10, 5));
        this.addButton(new WhiteKeyWidget(this.x+78, this.y+44, 11, 1));
        this.addButton(new WhiteKeyWidget(this.x+88, this.y+44, 13, 3));
        this.addButton(new WhiteKeyWidget(this.x+98, this.y+44, 15, 4));
        this.addButton(new WhiteKeyWidget(this.x+108, this.y+44, 17, 5));
        this.addButton(new WhiteKeyWidget(this.x+118, this.y+44, 18, 1));
        this.addButton(new WhiteKeyWidget(this.x+128, this.y+44, 20, 2));
        this.addButton(new WhiteKeyWidget(this.x+138, this.y+44, 22, 5));
        this.addButton(new WhiteKeyWidget(this.x+148, this.y+44, 23, 1));
        this.addButton(new BlackKeyWidget(this.x+12, this.y+28, 0));
        this.addButton(new BlackKeyWidget(this.x+23, this.y+28, 2));
        this.addButton(new BlackKeyWidget(this.x+34, this.y+28, 4));
        this.addButton(new BlackKeyWidget(this.x+52, this.y+28, 7));
        this.addButton(new BlackKeyWidget(this.x+64, this.y+28, 9));
        this.addButton(new BlackKeyWidget(this.x+82, this.y+28, 12));
        this.addButton(new BlackKeyWidget(this.x+93, this.y+28, 14));
        this.addButton(new BlackKeyWidget(this.x+104, this.y+28, 16));
        this.addButton(new BlackKeyWidget(this.x+122, this.y+28, 19));
        this.addButton(new BlackKeyWidget(this.x+134, this.y+28, 21));
        this.addButton(new BlackKeyWidget(this.x+152, this.y+28, 24));

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
    abstract class PianoKeyWidget extends ClickableWidget {
        protected int note;
        protected boolean played;

        protected PianoKeyWidget(int x, int y, int note) {
            super(x, y, 9, 16, LiteralText.EMPTY);
            this.note = note;
        }

        @Override
        public void onClick(double mouseX, double mouseY){

            pressedKey = this;
            played = true;

            if (pos==null) {
                 pos = screenHandler.getSyncedPos();
            }

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(note);
            buf.writeBlockPos(pos);

            ClientPlayNetworking.send(BlockTuner.TUNE_PACKET, buf);

            client.player.swingHand(Hand.MAIN_HAND);

            if (hasControlDown()){
                client.player.closeHandledScreen();
            }

        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            played = false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.active && this.visible) {
                if (this.isValidClickButton(button)) {
                    boolean bl = this.clicked(mouseX, mouseY);
                    if (bl) {
                        this.onClick(mouseX, mouseY);
                        return true;
                    }
                }

            }
            return false;
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
            if (played) {
                status = 1;
            } else if (this.isHovered()) {
                status = 2;
            }

            this.drawTexture(matrices, this.x, this.y - 10, 176 + 16 * status, 0, 9, 26);
        }
    }

    class WhiteKeyWidget extends PianoKeyWidget{
        private int keyType;

        public WhiteKeyWidget(int x, int y, int note, int keyType) {
            super(x, y, note);
            this.keyType = keyType;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (played) {
                status = 1;
            } else if (this.isHovered()) {
                status = 2;
            }

            this.drawTexture(matrices, this.x, this.y - 22, 176 + 16 * status, 40 * keyType, 9, 38);
        }

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        this.setDragging(false);
        if (pressedKey != null) {
            return pressedKey.mouseReleased(mouseX, mouseY, button);
        }
        else {
            return super.mouseReleased(mouseX, mouseY, button);
        }

    }

}
