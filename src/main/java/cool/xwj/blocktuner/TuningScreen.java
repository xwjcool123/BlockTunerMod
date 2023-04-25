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
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import javax.sound.midi.*;

@Environment(EnvType.CLIENT)
public class TuningScreen extends Screen {

    private final BlockPos pos;
    private final String commandPrefix;
    private PianoKeyWidget pressedKey = null;
    private final PianoKeyWidget[] pianoKeys = new PianoKeyWidget[25];
    private MidiDevice currentDevice;
    private final MidiReceiver receiver;
    private boolean configChanged = false;
    private Text deviceName;
    private boolean deviceAvailable = true;
    protected int backgroundWidth = 256;
    protected int backgroundHeight = 112;
    protected int x;
    protected int y;
    protected static final Text PLAY_MODE_TOGGLE_TOOLTIP = Text.translatable("settings.blocktuner.play_mode");
    protected static final Text KEY_TO_PIANO_TOGGLE_TOOLTIP =Text.translatable("settings.blocktuner.key_to_piano");
    protected static final Text EMPTY_MIDI_DEVICE = Text.translatable("midi_device.empty");
    protected static final Text MIDI_DEVICE_REFRESH_TOOLTIP = Text.translatable("settings.blocktuner.refresh");


    static final Identifier TEXTURE = new Identifier("blocktuner", "textures/gui/container/tune.png");

    public TuningScreen(Text title, BlockPos pos) {
        super(title);
        this.pos = pos;
        this.commandPrefix = "tune " + pos.getX() + ' ' + pos.getY() + ' ' + pos.getZ() + ' ';

        currentDevice = BlockTunerClient.getCurrentDevice();
        receiver = new MidiReceiver();
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        // Fancy(?) keyboard

        this.addDrawableChild(new WhiteKeyWidget(this.x + 16, this.y + 65, 1, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 32, this.y + 65, 3, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 48, this.y + 65, 5, 2));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 64, this.y + 65, 6, 0));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 80, this.y + 65, 8, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 96, this.y + 65, 10, 2));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 112, this.y + 65, 11, 0));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 128, this.y + 65, 13, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 144, this.y + 65, 15, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 160, this.y + 65, 17, 2));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 176, this.y + 65, 18, 0));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 192, this.y + 65, 20, 1));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 208, this.y + 65, 22, 2));
        this.addDrawableChild(new WhiteKeyWidget(this.x + 224, this.y + 65, 23, 0));
        this.addDrawableChild(new BlackKeyWidget(this.x + 8, this.y + 40, 0));
        this.addDrawableChild(new BlackKeyWidget(this.x + 24, this.y + 40, 2));
        this.addDrawableChild(new BlackKeyWidget(this.x + 40, this.y + 40, 4));
        this.addDrawableChild(new BlackKeyWidget(this.x + 72, this.y + 40, 7));
        this.addDrawableChild(new BlackKeyWidget(this.x + 88, this.y + 40, 9));
        this.addDrawableChild(new BlackKeyWidget(this.x + 120, this.y + 40, 12));
        this.addDrawableChild(new BlackKeyWidget(this.x + 136, this.y + 40, 14));
        this.addDrawableChild(new BlackKeyWidget(this.x + 152, this.y + 40, 16));
        this.addDrawableChild(new BlackKeyWidget(this.x + 184, this.y + 40, 19));
        this.addDrawableChild(new BlackKeyWidget(this.x + 200, this.y + 40, 21));
        this.addDrawableChild(new BlackKeyWidget(this.x + 232, this.y + 40, 24));

        this.addDrawableChild(new PlayModeToggle(this.x + 184, this.y + 8));
        this.addDrawableChild(new KeyToPianoToggle(this.x + 200, this.y + 8));
        this.addDrawableChild(new MidiSwitch(this.x + 216, this.y + 8));
        this.addDrawableChild(new MidiDeviceRefreshButton(this.x + 232, this.y + 8));

        this.addDrawable(new KeySignature(this.x + 112, this.y + 8));
        this.addDrawableChild(new KeyAddSharpButton(this.x + 144, this.y + 8));
        this.addDrawableChild(new KeyAddFlatButton(this.x + 144, this.y + 16));

        if (currentDevice != null && !currentDevice.isOpen()) {
            openCurrentDevice();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.drawBackground(matrices, delta, mouseX, mouseY);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (client == null || client.world == null || client.world.getBlockState(pos).getBlock() != Blocks.NOTE_BLOCK) {
            this.close();
        }
    }

    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        assert this.client != null;
        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    abstract class PianoKeyWidget extends ClickableWidget {
        private final int note;
        protected boolean played;

        protected PianoKeyWidget(int x, int y, int width, int height, int note) {
            super(x, y, width, height, Text.empty());
            this.note = note;
            pianoKeys[note] = this;
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if (this.hovered) {
                TuningScreen.this.renderTooltip(matrices, Text.literal(BlockTunerClient.getNoteName(note)), TuningScreen.this.x - 8 , TuningScreen.this.y - 2);
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY){

            pressedKey = this;
            played = true;

            if (client != null && client.player != null && client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendChatCommand(commandPrefix + note);
                client.player.swingHand(Hand.MAIN_HAND);
            }

            if (!BlockTunerConfig.isPlayMode()){
                close();
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
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
        }
    }

    class BlackKeyWidget extends PianoKeyWidget{
        public BlackKeyWidget(int x, int y, int note) {
            super(x, y, 16, 38, note);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (played) {
                status = 1;
            } else if (this.isHovered()) {
                status = 2;
            }
            drawTexture(matrices, this.getX(), this.getY(), 16 * status, 112, 16, 38);
        }
    }

    class WhiteKeyWidget extends PianoKeyWidget{
        private final int keyShape;

        public WhiteKeyWidget(int x, int y, int note, int keyShape) {
            super(x, y, 16, 38, note);
            this.keyShape = keyShape;
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if (!this.visible) {
                return;
            }
            boolean mask = mouseX >= this.getX() + 8 - 8 * keyShape && mouseY >= this.getY() && mouseX < this.getX() + 24 - 8 * keyShape && mouseY < this.getY() + 13;
            this.hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            this.hovered = this.hovered && !mask;

            this.renderButton(matrices, mouseX, mouseY, delta);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (played) {
                status = 1;
            } else if (this.isHovered()) {
                status = 2;
            }

            drawTexture(matrices, this.getX(), this.getY(), 16 * status + 48 * keyShape + 48, 112, 16, 38);
        }

        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            return this.active && this.visible && this.hovered;
        }

    }

    class PlayModeToggle extends ClickableWidget{
        public PlayModeToggle(int x, int y) {
            super(x, y, 16, 16, Text.empty());
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if (this.hovered) {
                TuningScreen.this.renderTooltip(matrices, PLAY_MODE_TOGGLE_TOOLTIP, TuningScreen.this.x - 8 , TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerConfig.isPlayMode()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }
            drawTexture(matrices, this.getX(), this.getY(), 192 + 16 * status, 112, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY){
            BlockTunerConfig.togglePlayMode();
            configChanged = true;
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    class KeyToPianoToggle extends ClickableWidget{
        public KeyToPianoToggle(int x, int y) {
            super(x, y, 16, 16, Text.empty());
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if (this.hovered) {
                TuningScreen.this.renderTooltip(matrices, KEY_TO_PIANO_TOGGLE_TOOLTIP, TuningScreen.this.x - 8 , TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerConfig.isKeyToPiano()) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }
            drawTexture(matrices, this.getX(), this.getY(), 192 + 16 * status, 128, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY){
            BlockTunerConfig.toggleKeyToPiano();
            configChanged = true;
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    class MidiSwitch extends ClickableWidget{

        public MidiSwitch(int x, int y) {
            super(x, y, 16, 16, Text.empty());
            if (currentDevice == null) {
                deviceName = EMPTY_MIDI_DEVICE;
            } else {
                deviceName = Text.literal(currentDevice.getDeviceInfo().getName());
            }
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if (this.hovered) {
                TuningScreen.this.renderTooltip(matrices, Text.translatable("settings.blocktuner.midi_device", deviceName), TuningScreen.this.x - 8 , TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (BlockTunerClient.getDeviceIndex() > 0) {
                status = 2;
            }
            if (this.isHovered()) {
                status += 1;
            }
            if (!deviceAvailable) {
                status += 4;
            }
            drawTexture(matrices, this.getX(), this.getY(), 192 + 16 * (status % 4), 144 + 16 * (status / 4), 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY){

            if (currentDevice != null && currentDevice.isOpen()) {
                currentDevice.close();
            }

            BlockTunerClient.loopDeviceIndex();
            currentDevice = BlockTunerClient.getCurrentDevice();

            if (currentDevice != null) {
                BlockTunerConfig.setMidiDeviceName(currentDevice.getDeviceInfo().getName());
                deviceName = Text.literal(BlockTunerConfig.getMidiDeviceName());
                openCurrentDevice();
            } else {
                BlockTunerConfig.setMidiDeviceName("");
                deviceName = EMPTY_MIDI_DEVICE;
            }
            configChanged = true;
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    class MidiDeviceRefreshButton extends ClickableWidget{
        public MidiDeviceRefreshButton(int x, int y) {
            super(x, y, 16, 16, Text.empty());
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
            if (this.hovered) {
                TuningScreen.this.renderTooltip(matrices, MIDI_DEVICE_REFRESH_TOOLTIP, TuningScreen.this.x - 8 , TuningScreen.this.y - 2);
            }
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (this.isHovered()) {
                status += 1;
            }
            drawTexture(matrices, this.getX(), this.getY(), 192 + 16 * status, 176, 16, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY){
            BlockTunerClient.refreshMidiDevice();
            if (currentDevice != null) {
                openCurrentDevice();
            }
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
        }
    }

    static class KeySignature extends DrawableHelper implements Drawable {

        public int x;
        public int y;

        public KeySignature(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            int keySignature = BlockTunerConfig.getKeySignature();
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexture(matrices, this.x, this.y, (keySignature + 8) % 8 * 32, (keySignature + 8) / 8 * 16 + 224, 32, 16);
        }
    }

    class KeyAddSharpButton extends ClickableWidget {
        public KeyAddSharpButton(int x, int y) {
            super(x, y, 8, 8, Text.empty());
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (this.isHovered()) {
                status += 1;
            }
            drawTexture(matrices, this.getX(), this.getY(), 8 * status, 152, 8, 8);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            BlockTunerConfig.keyAddSharp();
            configChanged = true;
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    class KeyAddFlatButton extends ClickableWidget{
        public KeyAddFlatButton(int x, int y) {
            super(x, y, 8, 8, Text.empty());
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int status = 0;
            if (this.isHovered()) {
                status += 1;
            }
            drawTexture(matrices, this.getX(), this.getY(), 8 * status + 16, 152, 8, 8);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            BlockTunerConfig.keyAddFlat();
            configChanged = true;
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {}
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (BlockTunerConfig.isKeyToPiano() && keyCode != 256) {
            int note = keyToNote(scanCode);
            if (note >= 0 && note <= 24 && !pianoKeys[note].played) {
                pianoKeys[note].onClick(0, 0);
            }
            return true;
        } else {
            if (keyCode == 69) {
                this.close();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        int note = keyToNote(scanCode);
        if (note >= 0 && note <= 24) {
            pianoKeys[note].onRelease(0, 0);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public void close() {
        if (currentDevice != null && currentDevice.isOpen()) {
            currentDevice.close();
        }
        receiver.close();
        if (configChanged) {
            BlockTunerConfig.save();
        }
        super.close();
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

    protected void openCurrentDevice(){
        try {
            currentDevice.open();
            deviceAvailable = true;
            currentDevice.getTransmitter().setReceiver(receiver);
        } catch (MidiUnavailableException e) {
            deviceAvailable = false;
            BlockTuner.LOGGER.info("[BlockTuner] MIDI device \"" + currentDevice.getDeviceInfo().getName() + "\" is currently unavailable. Is it busy or unplugged?");
        }
    }

    protected static int keyToNote(int scanCode) {
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
