package co.ata.pathcraft.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import co.ata.pathcraft.FeatureInstance;
import co.ata.pathcraft.PathCraft;
import co.ata.pathcraft.PathCraftClient;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.data.ClientPathData;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CharacterScreen extends Screen {

    int backgroundWidth = 256;
    int backgroundHeight = 164;

    public CharacterScreen() {
        super(Text.translatable("screen.pathcraft:character"));
        this.width = 256;
        this.height = 256;
    }

    private static final Identifier TEXTURE = new Identifier("pathcraft", "textures/gui/character.png");

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

    private void renderBio(MatrixStack matricies, ClientPathData data, int x, int y) {
        // Name
        this.textRenderer.draw(matricies, PathCraftClient.MC.player.getDisplayName(), x + 63, y + 9, 0x3f3f3f);
        
        // Ancestry
        this.textRenderer.draw(matricies, data.ancestry.getName(), x + 70, y + 18, 0x3f3f3f);

        // TODO: Class
        this.textRenderer.draw(matricies, data.clss.getName(), x + 70, y + 27, 0x3f3f3f);

        // Lots of empty space here. Not sure what to fill it with.
    }

    private void renderAbilities(MatrixStack matricies, ClientPathData data, int x, int y) {
        this.textRenderer.draw(matricies, "STR:", x + 16, y +  9, 0x3f3f3f);
        this.textRenderer.draw(matricies, "DEX:", x + 16, y + 18, 0x3f3f3f);
        this.textRenderer.draw(matricies, "CON:", x + 16, y + 27, 0x3f3f3f);
        this.textRenderer.draw(matricies, "INT:", x + 16, y + 36, 0x3f3f3f);
        this.textRenderer.draw(matricies, "WIS:", x + 16, y + 45, 0x3f3f3f);
        this.textRenderer.draw(matricies, "CHA:", x + 16, y + 54, 0x3f3f3f);

        this.textRenderer.draw(matricies, String.valueOf(data.stats.get(Stat.STRENGTH)),        x + 42, y +  9, 0x3f3f3f);
        this.textRenderer.draw(matricies, String.valueOf(data.stats.get(Stat.DEXTERITY)),       x + 42, y + 18, 0x3f3f3f);
        this.textRenderer.draw(matricies, String.valueOf(data.stats.get(Stat.CONSTITUTION)),    x + 42, y + 27, 0x3f3f3f);
        this.textRenderer.draw(matricies, String.valueOf(data.stats.get(Stat.INTELLIGENCE)),    x + 42, y + 36, 0x3f3f3f);
        this.textRenderer.draw(matricies, String.valueOf(data.stats.get(Stat.WISDOM)),          x + 42, y + 45, 0x3f3f3f);
        this.textRenderer.draw(matricies, String.valueOf(data.stats.get(Stat.CHARISMA)),        x + 42, y + 54, 0x3f3f3f);
    }
    
    int featListScroll = 0;
    private static final int maxLines = 9;
    int selectedFeat = -1;

    private void renderFeatures(MatrixStack matricies, ClientPathData data, int x, int y) {
        int featCount = data.features.size();
        if (featCount < maxLines)
            featListScroll = 0;
        int featStartIndex = featListScroll;
        int featEndIndex = Math.min(featStartIndex + maxLines, featCount);
        for (int i = featStartIndex; i < featEndIndex; i++) {
            StringVisitable featName = textRenderer.trimToWidth(data.features.get(i).getName(), 59);
            this.textRenderer.draw(matricies, featName.getString(), x + 8, y + 68 + (i - featStartIndex) * 10,
                    i == selectedFeat ? 0xFFFFFF : 0x808080);
        }

        // Draw the scrollbar
        float linesFraction = (float) maxLines / featCount;
        if(featCount < maxLines)
            linesFraction = 1;
        int scrollHeight = (int) (linesFraction * maxLines * 10);
        int scrollY = (int) (featListScroll * (maxLines * 10 - scrollHeight));
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matricies, x + 68, y + 67 + scrollY, 0, 164 + (90 - scrollHeight), 8, scrollHeight);
    }
    
    int descriptionScroll = 0;

    private List<OrderedText> DescriptionLines() {
        if (selectedFeat == -1)
            return new ArrayList<OrderedText>();
        FeatureInstance selected = PathCraftClient.data.features.get(selectedFeat);
        ArrayList<OrderedText> description = new ArrayList<OrderedText>();
        description.addAll(textRenderer.wrapLines(selected.getName(), 159));
        description.addAll(textRenderer.wrapLines(selected.getDescription(), 159));
        return description;
    }

    private void renderDescription(MatrixStack matricies, ClientPathData data, int x, int y) {
        if (selectedFeat == -1)
            return;
        List<OrderedText> description = DescriptionLines();
        int lines = description.size();
        if (lines < maxLines)
            descriptionScroll = 0;
        int lineStartIndex = descriptionScroll;
        int lineEndIndex = Math.min(lineStartIndex + maxLines, lines);
        for (int i = lineStartIndex; i < lineEndIndex; i++) {
            this.textRenderer.draw(matricies, description.get(i), x + 81, y + 68 + (i - lineStartIndex) * 10, 0xFFFFFF);
        }

        // Draw the scrollbar
        float linesFraction = (float) maxLines / lines;
        if (lines < maxLines)
            linesFraction = 1;
        int scrollHeight = (int) (linesFraction * maxLines * 10);
        int scrollY = (int) (descriptionScroll * (maxLines * 10 - scrollHeight));
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matricies, x + 241, y + 67 + scrollY, 0, 164 + (90 - scrollHeight), 8, scrollHeight);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        PathCraft.LOGGER.info("Mouse clicked: " + mouseX + ", " + mouseY + ", " + button);
        if (button != 0)
            return super.mouseClicked(mouseX, mouseY, button);
        ClientPathData data = PathCraftClient.data;
        int featCount = data.features.size();
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;
        if (mouseX >= x + 7 && mouseX <= x + 7 + 61 && mouseY >= y + 67 && mouseY <= y + 67 + maxLines * 10) {
            int index = (int) ((mouseY - y - 68) / 10);
            index += featListScroll;
            selectedFeat = -1;
            PathCraft.LOGGER.info("TRIED TO CLICK: " + index + "/" + featCount);
            if (index < featCount)
                	selectedFeat = index;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;
        ClientPathData data = PathCraftClient.data;
        if (mouseX >= x + 7 && mouseX <= x + 7 + 69 && mouseY >= y + 67 && mouseY <= y + 67 + maxLines * 10) {
            int lineCount = data.features.size();
            if(lineCount <= maxLines){
                featListScroll = 0;
                return true;
            }
            if (amount < 0) {
                featListScroll++;
                if (featListScroll > lineCount - maxLines)
                featListScroll = 1;
            } else {
                featListScroll--;
                if (featListScroll < 0)
                featListScroll = 0;
            }
            return true;
        }
        if (mouseX >= x + 80 && mouseX <= x + 80 + 169 && mouseY >= y + 67 && mouseY <= y + 67 + maxLines * 10) {
            if(selectedFeat == -1)
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

        ClientPathData data = PathCraftClient.data;
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        // Bio
        renderBio(matricies, data, x, y);

        // Attribute Numbers
        renderAbilities(matricies, data, x, y);

        // Feat List
        renderFeatures(matricies, data, x, y);
        
        // Feat Description
        renderDescription(matricies, data, x, y);
    }

}