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

package cool.xwj.blocktuner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
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

    static final Identifier TEXTURE = new Identifier("blocktuner", "textures/gui/container/tune.png");

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

        this.addDrawableChild(new WhiteKeyWidget(this.x + 18, this.y + 48, 1, 3));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 28, this.y + 48, 3, 4));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 38, this.y + 48, 5, 5));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 48, this.y + 48, 6, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 58, this.y + 48, 8, 2));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 68, this.y + 48, 10, 5));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 78, this.y + 48, 11, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 88, this.y + 48, 13, 3));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 98, this.y + 48, 15, 4));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 108, this.y + 48, 17, 5));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 118, this.y + 48, 18, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 128, this.y + 48, 20, 2));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 138, this.y + 48, 22, 5));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 148, this.y + 48, 23, 1));
        this.addDrawableChild(new BlackKeyWidget(this.x + 12, this.y + 32, 0));
        this.addDrawableChild(new BlackKeyWidget(this.x + 23, this.y + 32, 2));
        this.addDrawableChild(new BlackKeyWidget(this.x + 34, this.y + 32, 4));
        this.addDrawableChild(new BlackKeyWidget(this.x + 52, this.y + 32, 7));
        this.addDrawableChild(new BlackKeyWidget(this.x + 64, this.y + 32, 9));
        this.addDrawableChild(new BlackKeyWidget(this.x + 82, this.y + 32, 12));
        this.addDrawableChild(new BlackKeyWidget(this.x + 93, this.y + 32, 14));
        this.addDrawableChild(new BlackKeyWidget(this.x + 104, this.y + 32, 16));
        this.addDrawableChild(new BlackKeyWidget(this.x + 122, this.y + 32, 19));
        this.addDrawableChild(new BlackKeyWidget(this.x + 134, this.y + 32, 21));
        this.addDrawableChild(new BlackKeyWidget(this.x + 152, this.y + 32, 24));

        this.addDrawableChild(new PlayModeToggle(this.x + 118, this.y - 16));
        this.addDrawableChild(new KeyToPianoToggle(this.x + 136, this.y - 16));
        midiSwitch = this.addDrawableChild(new MidiSwitch(this.x + 154, this.y - 16));

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
        if (/*this.client.player.getInventory().getCursorStack().isEmpty() &&*/ this.focusedSlot != null && this.focusedSlot.hasStack()) {
            this.renderTooltip(matrices, this.focusedSlot.getStack(), x, y);
        } else if (midiSwitch != null && midiSwitch.isHovered()) {
            this.renderTooltip(matrices, midiSwitch.getDeviceName(), x , y);
        }

    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.client != null;
        RenderSystem.setShaderTexture(0, TEXTURE);
//        this.client.getTextureManager().bindTexture(TEXTURE);
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
        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {
        }
    }

    class BlackKeyWidget extends PianoKeyWidget{
        public BlackKeyWidget(int x, int y, int note) {
            super(x, y, note);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
//            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
            RenderSystem.setShaderTexture(0, TEXTURE);
//            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (played) {
                status = 1;
            } else if (this.isHovered()) {
                status = 2;
            }

            this.drawTexture(matrices, this.x, this.y - 22, 176 + 16 * status, 40 * keyType, 9, 38);
        }

    }

    abstract static class ToggleWidget extends ClickableWidget{
        public ToggleWidget(int x, int y, int width, int height, Text message) {
            super(x, y, width, height, message);
        }

        @Override
        public void appendNarrations(NarrationMessageBuilder builder) {

        }
    }
    static class PlayModeToggle extends ToggleWidget{
        public PlayModeToggle(int x, int y) {
            super(x, y, 16, 16, LiteralText.EMPTY);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
//            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerClient.isPlayMode()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }

            this.drawTexture(matrices, this.x, this.y, 224, 16 * status, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY){
            BlockTunerClient.togglePlayMode();
        }

    }

    static class KeyToPianoToggle extends ToggleWidget{
        public KeyToPianoToggle(int x, int y) {
            super(x, y, 16, 16, LiteralText.EMPTY);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
//            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerClient.isKeyToPiano()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }

            this.drawTexture(matrices, this.x, this.y, 240, 16 * status, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY){
            BlockTunerClient.toggleKeyToPiano();
        }

    }

    class MidiSwitch extends ToggleWidget{

        private Text deviceName;
        private boolean available = true;

        public MidiSwitch(int x, int y) {
            super(x, y, 16, 16, LiteralText.EMPTY);
            if (currentDevice == null) {
                deviceName = new TranslatableText("midi_device.empty");
            } else {
                deviceName = new LiteralText(currentDevice.getDeviceInfo().getName());
            }
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
//            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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

            this.drawTexture(matrices, this.x, this.y, 224, 64 + 16 * status, 16, 16);
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
                    BlockTuner.LOGGER.info("[BlockTuner] MIDI device \"" + deviceName.toString() + "\" is currently unavailable. Is it busy or unplugged?");
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
        return switch (scanCode) {
            case 3, 38 -> 7;
            case 4, 39 -> 9;
            case 6 -> 12;
            case 7 -> 14;
            case 8 -> 16;
            case 10 -> 19;
            case 11 -> 21;
            case 13 -> 24;
            case 16, 51 -> 6;
            case 17, 52 -> 8;
            case 18, 53 -> 10;
            case 19 -> 11;
            case 20 -> 13;
            case 21 -> 15;
            case 22 -> 17;
            case 23 -> 18;
            case 24 -> 20;
            case 25 -> 22;
            case 26 -> 23;
            case 34 -> 0;
            case 35 -> 2;
            case 36 -> 4;
            case 48 -> 1;
            case 49 -> 3;
            case 50 -> 5;
            default -> -1;
        };
    }

}
