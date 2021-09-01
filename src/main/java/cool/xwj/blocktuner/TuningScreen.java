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
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import javax.sound.midi.*;

public class TuningScreen extends HandledScreen<ScreenHandler> {

    TuningScreenHandler screenHandler;
    private BlockPos pos = null;
    private PianoKeyWidget pressedKey = null;
    private final PianoKeyWidget[] pianoKeys = new PianoKeyWidget[25];
    private MidiSwitch midiSwitch = null;
    private MidiDevice currentDevice;
    private final MidiReceiver receiver;


    private static final Identifier TEXTURE = new Identifier("blocktuner", "textures/gui/container/tune.png");

    public TuningScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (TuningScreenHandler) handler;
        currentDevice = BlockTunerClient.getCurrentDevice();
        receiver = new MidiReceiver();
    }

    @Override
    protected void init() {
        super.init();

        // Fancy(?) keyboard

        this.addButton(new WhiteKeyWidget(this.x + 18, this.y + 48, 1, 3));
        this.addButton(new WhiteKeyWidget(this.x + 28, this.y + 48, 3, 4));
        this.addButton(new WhiteKeyWidget(this.x + 38, this.y + 48, 5, 5));
        this.addButton(new WhiteKeyWidget(this.x + 48, this.y + 48, 6, 1));
        this.addButton(new WhiteKeyWidget(this.x + 58, this.y + 48, 8, 2));
        this.addButton(new WhiteKeyWidget(this.x + 68, this.y + 48, 10, 5));
        this.addButton(new WhiteKeyWidget(this.x + 78, this.y + 48, 11, 1));
        this.addButton(new WhiteKeyWidget(this.x + 88, this.y + 48, 13, 3));
        this.addButton(new WhiteKeyWidget(this.x + 98, this.y + 48, 15, 4));
        this.addButton(new WhiteKeyWidget(this.x + 108, this.y + 48, 17, 5));
        this.addButton(new WhiteKeyWidget(this.x + 118, this.y + 48, 18, 1));
        this.addButton(new WhiteKeyWidget(this.x + 128, this.y + 48, 20, 2));
        this.addButton(new WhiteKeyWidget(this.x + 138, this.y + 48, 22, 5));
        this.addButton(new WhiteKeyWidget(this.x + 148, this.y + 48, 23, 1));
        this.addButton(new BlackKeyWidget(this.x + 12, this.y + 32, 0));
        this.addButton(new BlackKeyWidget(this.x + 23, this.y + 32, 2));
        this.addButton(new BlackKeyWidget(this.x + 34, this.y + 32, 4));
        this.addButton(new BlackKeyWidget(this.x + 52, this.y + 32, 7));
        this.addButton(new BlackKeyWidget(this.x + 64, this.y + 32, 9));
        this.addButton(new BlackKeyWidget(this.x + 82, this.y + 32, 12));
        this.addButton(new BlackKeyWidget(this.x + 93, this.y + 32, 14));
        this.addButton(new BlackKeyWidget(this.x + 104, this.y + 32, 16));
        this.addButton(new BlackKeyWidget(this.x + 122, this.y + 32, 19));
        this.addButton(new BlackKeyWidget(this.x + 134, this.y + 32, 21));
        this.addButton(new BlackKeyWidget(this.x + 152, this.y + 32, 24));

        this.addButton(new PlayModeToggle(this.x + 128, this.y + 7));
        this.addButton(new KeyToPianoToggle(this.x + 142, this.y + 7));
        midiSwitch = this.addButton(new MidiSwitch(this.x + 159, this.y + 7));

        if (currentDevice != null && !currentDevice.isOpen()) {
            try {
                currentDevice.open();
                midiSwitch.available = true;
                currentDevice.getTransmitter().setReceiver(receiver);
            } catch (MidiUnavailableException e) {
                midiSwitch.available = false;
            }
        }

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(MatrixStack matrices, int x, int y) {
        assert this.client != null;
        assert this.client.player != null;
        if (this.client.player.inventory.getCursorStack().isEmpty() && this.focusedSlot != null && this.focusedSlot.hasStack()) {
            this.renderTooltip(matrices, this.focusedSlot.getStack(), x, y);
        } else if (midiSwitch != null && midiSwitch.isHovered()) {
            this.renderTooltip(matrices, midiSwitch.getDeviceName(), x, y);
        }

    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.client != null;
        this.client.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Environment(EnvType.CLIENT)
    abstract class PianoKeyWidget extends ClickableWidget {
        private final int note;
        protected boolean played;

        protected PianoKeyWidget(int x, int y, int note) {
            super(x, y, 9, 16, LiteralText.EMPTY);
            this.note = note;
            pianoKeys[note] = this;
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

            assert client != null;
            assert client.player != null;
            client.player.swingHand(Hand.MAIN_HAND);

            if (!BlockTunerClient.isPlayMode()){
                onClose();
            }

        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            played = false;
            pressedKey = null;
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
        private final int keyType;

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

    static class PlayModeToggle extends ClickableWidget{
        public PlayModeToggle(int x, int y) {
            super(x, y, 9, 9, LiteralText.EMPTY);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerClient.isPlayMode()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }

            this.drawTexture(matrices, this.x, this.y, 224, 16 * status, 9, 11);
        }

        @Override
        public void onClick(double mouseX, double mouseY){
            BlockTunerClient.togglePlayMode();
        }

    }

    static class KeyToPianoToggle extends ClickableWidget{
        public KeyToPianoToggle(int x, int y) {
            super(x, y, 12, 9, LiteralText.EMPTY);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerClient.isKeyToPiano()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }

            this.drawTexture(matrices, this.x, this.y, 240, 16 * status, 11, 11);
        }

        @Override
        public void onClick(double mouseX, double mouseY){
            BlockTunerClient.toggleKeyToPiano();
        }

    }

    class MidiSwitch extends ClickableWidget{

        private Text deviceName;
        private boolean available = true;

        public MidiSwitch(int x, int y) {
            super(x, y, 9, 9, LiteralText.EMPTY);
            if (currentDevice == null) {
                deviceName = new TranslatableText("midi_device.empty");
            } else {
                deviceName = new LiteralText(currentDevice.getDeviceInfo().getName());
            }
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerClient.getDeviceIndex() > 0) {
                status = 2;
                if (!available) {
                    status = 4;
                }
            }
            if (this.isHovered()) {
                status += 1;
            }

            this.drawTexture(matrices, this.x, this.y, 224, 64 + 16 * status, 11, 11);
        }

        public Text getDeviceName() {
            return deviceName;
        }

        @Override
        public void onClick(double mouseX, double mouseY){

            if (currentDevice != null && currentDevice.isOpen()) {
                currentDevice.close();
            }

            BlockTunerClient.loopDeviceIndex();
            currentDevice = BlockTunerClient.getCurrentDevice();

            if (currentDevice != null) {
                deviceName = new LiteralText(currentDevice.getDeviceInfo().getName());
                try {
                    currentDevice.open();
                    available = true;
                    currentDevice.getTransmitter().setReceiver(receiver);
                } catch (MidiUnavailableException e) {
                    available = false;
                }
            } else {
                deviceName = new TranslatableText("midi_device.empty");
            }
        }

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        this.setDragging(false);
        if (pressedKey != null) {
            return pressedKey.mouseReleased(mouseX, mouseY, button);
        } else {
            return super.mouseReleased(mouseX, mouseY, button);
        }

    }

    @Override
    public void tick() {
        super.tick();
        // something may be here in the future.
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){

        if (BlockTunerClient.isKeyToPiano() && keyCode != 256) {
            int note = keyToNote(scanCode);
            if (note >= 0 && note <= 24 && !pianoKeys[note].played) {
                pianoKeys[note].onClick(0, 0);
            }

            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers){
        int note = keyToNote(scanCode);
        if (note >= 0 && note <= 24) {
            pianoKeys[note].onRelease(0, 0);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        if (currentDevice != null && currentDevice.isOpen()) {
            currentDevice.close();
        }
        receiver.close();
        super.onClose();
    }

    class MidiReceiver implements Receiver {
        public MidiReceiver() {}
        public void send(MidiMessage msg, long timeStamp) {
            byte[] message = msg.getMessage();
            if (message.length == 3 && message[0] <= -97 && message[1] >= 54 && message[1] <= 78) {
                assert client != null;
                if (message[0] >= -112 && message[2] != 0) {

                    // MIDI note on
                    client.execute(()-> pianoKeys[message[1] - 54].onClick(0, 0));

                } else {

                    // MIDI note off
                    client.execute(()-> pianoKeys[message[1] - 54].onRelease(0, 0));

                }
            }
        }
        public void close() {}
    }

    protected int keyToNote(int scanCode) {
        int note = -1;
        switch (scanCode) {
            case 3 :
            case 38:
                note = 7;
                break;
            case 4 :
            case 39 :
                note = 9;
                break;
            case 6 :
                note = 12;
                break;
            case 7 :
                note = 14;
                break;
            case 8 :
                note = 16;
                break;
            case 10 :
                note = 19;
                break;
            case 11 :
                note = 21;
                break;
            case 13 :
                note = 24;
                break;
            case 16 :
            case 51 :
                note = 6;
                break;
            case 17 :
            case 52 :
                note = 8;
                break;
            case 18 :
            case 53 :
                note = 10;
                break;
            case 19 :
                note = 11;
                break;
            case 20 :
                note = 13;
                break;
            case 21 :
                note = 15;
                break;
            case 22 :
                note = 17;
                break;
            case 23 :
                note = 18;
                break;
            case 24 :
                note = 20;
                break;
            case 25 :
                note = 22;
                break;
            case 26 :
                note = 23;
                break;
            case 34 :
                note = 0;
                break;
            case 35 :
                note = 2;
                break;
            case 36 :
                note = 4;
                break;
            case 48 :
                note = 1;
                break;
            case 49 :
                note = 3;
                break;
            case 50 :
                note = 5;
                break;
        }
        return note;
    }

}
