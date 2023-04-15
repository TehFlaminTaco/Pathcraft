package co.ata.pathcraft.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import co.ata.pathcraft.AbilityInstance;
import co.ata.pathcraft.PathCraft;
import co.ata.pathcraft.PathCraftClient;
import co.ata.pathcraft.Spell;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AbilityBookScreen extends Screen {
    int backgroundWidth = 146;
    int backgroundHeight = 180;

    static final int bookU = 20;
    static final int bookV = 1;

    static final int titleEndX = 127;
    static final int titleEndY = 15;
    static final int textStartX = 16;
    static final int textStartY = 29;
    static final int backButtonX = 26;
    static final int backButtonY = 158;
    static final int nextButtonX = 99;
    static final int nextButtonY = 158;

    static final int buttonU = 3;
    static final int buttonV = 194;
    static final int buttonW = 23;
    static final int buttonH = 13;


    static int selectedPage = 0;

    ArrayList<ArrayList<AbilityInstance>> pages = new ArrayList<ArrayList<AbilityInstance>>();

    public AbilityBookScreen() {
        super(Text.translatable("screen.pathcraft:abilitybook"));
        this.width = 256;
        this.height = 256;

        List<AbilityInstance> allAbilities = PathCraftClient.data.features.stream()
            .flatMap(f -> f.providedAbilities.stream())
            .collect(java.util.stream.Collectors.toList());
        
        pages.add(new ArrayList<AbilityInstance>());

        allAbilities.sort((a, b) -> a.getName().getString().compareTo(b.getName().getString()));
        for(AbilityInstance a : allAbilities){
            if(a.ability instanceof Spell){
                Spell s = (Spell)a.ability;
                while(pages.size() <= s.slotLevel + 1)
                    pages.add(new ArrayList<AbilityInstance>());
                pages.get(s.slotLevel + 1).add(a);
            } else {
                pages.get(0).add(a);
            }
        }
    }
    public static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/book.png");

    public Text CategoryName(int index) {
        if (index == 0)
            return Text.translatable("screen.pathcraft:abilitybook.category.0");
        else if (index == 1)
            return PathCraftClient.data.clss.cantripName();
        return Text.translatable("screen.pathcraft:abilitybook.category.1", index - 1);
    }

    private int CategoryFirstPage(int category) {
        int page = 0;
        for (int i = 0; i < category; i++)
            page += pages.get(i).size() / 14 + 1;
        return page;
    }

    private int CategoryLastPage(int category) {
        return CategoryFirstPage(category + 1) - 1;
    }
    
    private int PageToCategory(int page){
        int category = 0;
        while(CategoryFirstPage(category + 1) <= page)
            category++;
        return category;
    }
    
    @Override
    public void renderBackground(MatrixStack matrices) {
        super.renderBackground(matrices);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;
        // Draw two, one to the right and one flipped to the left.
        drawTexture(matrices, x, y, bookU, bookV, backgroundWidth, backgroundHeight);
    }

    boolean IsOver(int x, int y, int w, int h, int mx, int my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void Click(){
        client.getSoundManager().play(
            PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1F, 0.7F)
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;
        if (selectedPage > 0
                && IsOver(x + backButtonX, y + backButtonY, buttonW, buttonH, (int) mouseX, (int) mouseY)) {
            selectedPage--;
            Click();
            return true;
        }
        if (selectedPage < CategoryLastPage(pages.size() - 1)
                && IsOver(x + nextButtonX, y + nextButtonY, buttonW, buttonH, (int) mouseX, (int) mouseY)) {
            selectedPage++;
            Click();
            return true;
        }
        int cat = PageToCategory(selectedPage);
        int page = selectedPage - CategoryFirstPage(cat);
        int start = page * 14;
        int end = Math.min(start + 14, pages.get(cat).size());
        for (int i = start; i < end; i++) {
            AbilityInstance a = pages.get(cat).get(i);
            if (IsOver(x + textStartX, y + textStartY + (i - start) * 9, titleEndX, 9, (int) mouseX, (int) mouseY)) {
                Click();
                // Close the screen and send a packet to activate an ability (And activate it clientside);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        if (selectedPage > 0) {
            int u = buttonU;
            if (IsOver(x + backButtonX, y + backButtonY, buttonW, buttonH, mouseX, mouseY))
                u += buttonW;
            drawTexture(matrices, x + backButtonX, y + backButtonY, u, buttonV + buttonH, buttonW, buttonH);
        }
        if (selectedPage < CategoryLastPage(pages.size() - 1)) {
            int u = buttonU;
            if (IsOver(x + nextButtonX, y + nextButtonY, buttonW, buttonH, mouseX, mouseY))
                u += buttonW;
            drawTexture(matrices, x + nextButtonX, y + nextButtonY, u, buttonV, buttonW, buttonH);
        }

        Text categoryName = CategoryName(PageToCategory(selectedPage));
        int titleWidth = this.textRenderer.getWidth(categoryName);
        this.textRenderer.draw(matrices, this.CategoryName(
            PageToCategory(selectedPage)
        ), (x + titleEndX) - titleWidth, y + titleEndY, 0x000000);

        int cat = PageToCategory(selectedPage);
        int page = selectedPage - CategoryFirstPage(cat);
        int start = page * 14;
        int end = Math.min(start + 14, pages.get(cat).size());
        for (int i = start; i < end; i++) {
            AbilityInstance a = pages.get(cat).get(i);
            int row = i - start;
            int rowX = x + textStartX;
            int rowY = y + textStartY + row * 9;
            int col = 0x000000;
            int width = this.textRenderer.getWidth(a.getName());
            if(IsOver(rowX, rowY, width, 9, mouseX, mouseY))
                col = 0x3F3F3F;
            this.textRenderer.draw(matrices, a.getName(), rowX, rowY, col);
        }
    }
}
