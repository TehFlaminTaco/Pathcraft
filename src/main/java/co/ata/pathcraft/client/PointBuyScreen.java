package co.ata.pathcraft.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import co.ata.pathcraft.Feature;
import co.ata.pathcraft.PathCraftClient;
import co.ata.pathcraft.Stat;
import co.ata.pathcraft.data.ClientPathData;
import co.ata.pathcraft.network.PathNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PointBuyScreen extends Screen {
    int backgroundWidth = 80;
    int backgroundHeight = 90;

    List<ButtonWidget> subtractButtons = new ArrayList<ButtonWidget>();
    List<ButtonWidget> addButtons = new ArrayList<ButtonWidget>();

    HashMap<Stat, Integer> stats = new HashMap<Stat, Integer>();

    ButtonWidget acceptButton;

    public PointBuyScreen() {
        super(Text.translatable("screen.pathcraft:pointbuy"));
        this.width = 256;
        this.height = 256;
        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        int yOff = 19;

        ClientPathData data = PathCraftClient.data;

        for (Stat s : Stat.values()) {
            stats.put(s, data.stats.get(s));

            subtractButtons.add(ButtonWidget.builder(Text.of("-"), (button) -> {
                stats.put(s, stats.get(s) - 1);
            }).dimensions(x + 4, y + yOff, 9, 7).build());
            addButtons.add(ButtonWidget.builder(Text.of("+"), (button) -> {
                stats.put(s, stats.get(s) + 1);
            }).dimensions(x + 67, y + yOff, 9, 7).build());

            yOff += 9;
        }

        acceptButton = ButtonWidget.builder(Text.translatable("screen.pathcraft:pointbuy.accept"), (button) -> {
            //PathCraftClient.clientData.setStats(stats);
            PacketByteBuf buf = PacketByteBufs.create();
            for (Stat s : Stat.values()) {
                buf.writeInt(stats.get(s));
            }
            ClientPlayNetworking.send(PathNetwork.SetStats, buf);
            // Spend all PointBuy features on the client data
            PathCraftClient.data.features.stream().filter(f -> f.feature == Feature.POINT_BUY)
                    .forEach(f -> Feature.POINT_BUY.Spend(f));
            PathCraftClient.NextCharacterGenTick = 20 * 5;
            this.close();
        }).dimensions(x + 14, y + 75, 52, 9).build();
    }

    public static final Identifier TEXTURE = new Identifier("pathcraft", "textures/gui/pointbuy.png");

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ButtonWidget b : subtractButtons) {
            if (b.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        for (ButtonWidget b : addButtons) {
            if (b.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (acceptButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

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

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        int x = (this.width - backgroundWidth) / 2;
        int y = (this.height - backgroundHeight) / 2;

        String pointCount = "Points: " + Stat.TotalPoints(stats);
        int titleX = backgroundWidth/2 - textRenderer.getWidth(pointCount)/2;
        textRenderer.draw(matrices, pointCount, x + titleX, y + 5, 0x3F3F3F);

        int yOff = 19;
        int i = 0;
        ClientPathData data = PathCraftClient.data;
        for (Stat s : Stat.values()) {
            subtractButtons.get(i).active = Stat.CanBuy(stats, s, stats.get(s) - 1);
            subtractButtons.get(i).setPosition(x + 4, y + yOff);
            subtractButtons.get(i).render(matrices, mouseX, mouseY, delta);
            addButtons.get(i).active = Stat.CanBuy(stats, s, stats.get(s) + 1);
            addButtons.get(i).setPosition(x + 67, y + yOff);
            addButtons.get(i).render(matrices, mouseX, mouseY, delta);
            String statName = s.toString().substring(0, 3);
            textRenderer.draw(matrices, statName, x + 24, y + yOff, 0x3F3F3F);
            int FakeNumber = stats.get(s);
            int color = 0x3F3F3F;
            if (data.ancestry.RacialStats.containsKey(s)) {
                int racialBonus = data.ancestry.RacialStats.get(s);
                FakeNumber += racialBonus;
                if (racialBonus > 0) {
                    color = 0x00FF00;
                } else if (racialBonus < 0) {
                    color = 0xFF0000;
                }
            }
            textRenderer.draw(matrices, ""+FakeNumber, x + 50, y + yOff, color);
            yOff += 9;
            i++;
        }

        acceptButton.active = Stat.TotalPoints(stats) == 0;
        acceptButton.setPosition(x + 14, y + 75);
        acceptButton.render(matrices, mouseX, mouseY, delta);


        super.render(matrices, mouseX, mouseY, delta);
    }
}
