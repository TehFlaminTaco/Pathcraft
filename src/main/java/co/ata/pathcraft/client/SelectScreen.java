package co.ata.pathcraft.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import co.ata.pathcraft.PathCraftClient;
import co.ata.pathcraft.data.ClientPathData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SelectScreen<T> extends Screen {
    Text title;
    List<SelectOption<T>> options;
    Consumer<T> callback;

    int backgroundWidth = 256;
    int backgroundHeight = 115;

    ButtonWidget selectButton;
    
    public SelectScreen(Text title, List<SelectOption<T>> options, Consumer<T> callback) {
        super(Text.translatable("screen.pathcraft:select"));
        this.title = title;
        this.options = options;
        this.width = 256;
        this.height = 256;
        this.callback = callback;

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        selectButton = ButtonWidget.builder(Text.translatable("screen.pathcraft:select.select"), (button) -> {
            if (selected != -1){
                callback.accept(options.get(selected).value);
                this.close();
            }
        })
            .dimensions(x + 192, y + 4, 58, 11)
            .build();
    }

    private static final Identifier TEXTURE = new Identifier("pathcraft", "textures/gui/selection.png");
    
    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    private static final int maxLines = 9;
    int listScroll = 0;
    int selected = -1;

    private List<OrderedText> DescriptionLines() {
        if (selected == -1)
            return new ArrayList<OrderedText>();
        ArrayList<OrderedText> description = new ArrayList<OrderedText>();
        description.addAll(textRenderer.wrapLines(options.get(selected).name, 159));
        description.addAll(textRenderer.wrapLines(options.get(selected).description, 159));
        return description;
    }

    private void renderOptions(MatrixStack matricies, int x, int y) {
        int count = options.size();
        if (count < maxLines)
            listScroll = 0;
        int startIndex = listScroll;
        int endIndex = Math.min(startIndex + maxLines, count);
        for (int i = startIndex; i < endIndex; i++) {
            StringVisitable name = textRenderer.trimToWidth(options.get(i).name, 59);
            int color = 0x808080;
            if (options.get(i).disabled) {
                color = 0x800000;
                if(selected == i)
                    color = 0xFF0000;
            }else if(selected == i)
                color = 0xFFFFFF;
            this.textRenderer.draw(matricies, name.getString(), x + 8, y + 18 + (i - startIndex) * 10, color);
        }

        // Draw the scrollbar
        float linesFraction = (float) maxLines / count;
        if(count < maxLines)
            linesFraction = 1;
        int scrollHeight = (int) (linesFraction * maxLines * 10);
        int scrollY = (int) (listScroll * (maxLines * 10 - scrollHeight));
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matricies, x + 68, y + 17 + scrollY, 0, 115 + (90 - scrollHeight), 8, scrollHeight);
    }
    
    int descriptionScroll = 0;

    private void renderDescription(MatrixStack matricies, ClientPathData data, int x, int y) {
        if (selected == -1)
            return;
        List<OrderedText> description = DescriptionLines();
        int lines = description.size();
        if (lines < maxLines)
            descriptionScroll = 0;
        int lineStartIndex = descriptionScroll;
        int lineEndIndex = Math.min(lineStartIndex + maxLines, lines);
        for (int i = lineStartIndex; i < lineEndIndex; i++) {
            this.textRenderer.draw(matricies, description.get(i), x + 81, y + 18 + (i - lineStartIndex) * 10, 0xFFFFFF);
        }

        // Draw the scrollbar
        float linesFraction = (float) maxLines / lines;
        if(lines < maxLines)
            linesFraction = 1;
        int scrollHeight = (int) (linesFraction * maxLines * 10);
        int scrollY = (int) (descriptionScroll * (maxLines * 10 - scrollHeight));
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matricies, x + 241, y + 17 + scrollY, 0, 115 + (90 - scrollHeight), 8, scrollHeight);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0)
            return super.mouseClicked(mouseX, mouseY, button);
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;
        if (mouseX >= x + 7 && mouseX <= x + 7 + 61 && mouseY >= y + 17 && mouseY <= y + 17 + maxLines * 10) {
            int index = (int) ((mouseY - y - 18) / 10);
            index += (int) (listScroll * options.size());
            selected = -1;
            if (index < options.size())
                selected = index;
            return true;
        }
        if (selectButton.mouseClicked(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;
        if (mouseX >= x + 7 && mouseX <= x + 7 + 69 && mouseY >= y + 17 && mouseY <= y + 17 + maxLines * 10) {
            int lineCount = options.size();
            if(lineCount <= maxLines){
                listScroll = 0;
                return true;
            }
            if (amount < 0) {
                listScroll++;
                if (listScroll > lineCount - maxLines)
                    listScroll = 1;
            } else {
                listScroll--;
                if (listScroll < 0)
                    listScroll = 0;
            }
            return true;
        }
        if (mouseX >= x + 80 && mouseX <= x + 80 + 169 && mouseY >= y + 17 && mouseY <= y + 17 + maxLines * 10) {
            if(selected == -1)
                return true;
            int lineCount = DescriptionLines().size();
            if(lineCount <= maxLines){
                descriptionScroll = 0;
                return true;
            }
            if (amount < 0) {
                descriptionScroll++;
                if (descriptionScroll > lineCount - maxLines)
                    descriptionScroll = lineCount - maxLines;
            } else {
                descriptionScroll--;
                if (descriptionScroll < 0)
                    descriptionScroll = 0;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void render(MatrixStack matricies, int mouseX, int mouseY, float delta) {
        this.renderBackground(matricies);
        super.render(matricies, mouseX, mouseY, delta);

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        renderOptions(matricies, x, y);
        renderDescription(matricies, PathCraftClient.data, x, y);

        this.textRenderer.draw(matricies, title, x + 8, y + 6, 0x3f3f3f);

        selectButton.active = selected != -1 && !options.get(selected).disabled;
        selectButton.setPosition(x + 192, y + 4);
        selectButton.render(matricies, mouseX, mouseY, delta);
    }

}
