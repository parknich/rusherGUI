package org.example.theme.item.items;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Tuple;
import org.example.theme.ExamplePlugin;
import org.example.theme.Panel;
import org.lwjgl.glfw.GLFW;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.render.font.IFontRenderer;
import org.rusherhack.client.api.setting.BindSetting;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.client.api.ui.ElementBase;
import org.rusherhack.client.api.ui.ElementHandlerBase;
import org.rusherhack.client.api.ui.panel.IPanelItem;
import org.rusherhack.core.setting.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.rusherhack.client.api.Globals.mc;

public class ModuleItem extends ElementBase implements IPanelItem {
    IModule module;
    Panel panel;
    public boolean open = false;
    private final ArrayList<ExtendableItem> subItems = new ArrayList<>();
    private double rendererHeight;

    public ModuleItem(IModule module, Panel panel){
        this.module = module;
        this.panel = panel;

        addSettingItems(module.getSettings());

        rendererHeight = 11F;
    }
    @Override
    public double getX() {
        return panel.getX() + 1;
    }
    @Override
    public double getWidth() {
        return panel.getWidth() - 2;
    }

    @Override
    public double getHeight() {
        return rendererHeight;
    }

    @Override
    public double getHeight(boolean total) {
        if(total) return rendererHeight;
        return 11f;
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY, boolean includeSubItems) {
        return false;
    }

    @Override
    public void render(RenderContext context, double mouseX, double mouseY) {
        final IRenderer2D renderer = RusherHackAPI.getRenderer2D();
        final IFontRenderer fontRenderer = RusherHackAPI.fonts().getFontRenderer();
        possibleHeightUpdate();

        if(module instanceof ToggleableModule){
            if(((ToggleableModule) module).isToggled())
                renderer.drawOutlinedRectangle(getX(), getY(), getWidth(), getHeight(false), 3, ExamplePlugin.theme.getColorSetting().getValue().getRGB(), ExamplePlugin.theme.moduleOutlineColor.getValueRGB());
        }
        else{
            renderer.drawRectangle(getX(), getY(), getWidth(), getHeight(false), ExamplePlugin.theme.getColorSetting().getValue().getRGB());
        }



        if(!subItems.isEmpty() && open) {
            renderer.getMatrixStack().pushPose();
            double height = 11.5f;
            for (ExtendableItem subItem : subItems) {
                if(subItem.setting.isHidden()) continue;

                subItem.setX(getX());
                subItem.setY(getY() + height);

                subItem.render(context, mouseX, mouseY);
                height += subItem.getHeight(true) + 0.5F;
            }
            renderer.getMatrixStack().popPose();
        }

        if(panel.isHovering(mouseX,mouseY, getX(), getY(), getWidth(), getHeight(false))) {
            renderer.drawRectangle(getX(), getY(), getWidth(), getHeight(false), new Color(0, 0, 0, 50).getRGB());
            String description =
                    (module.getDescription().isEmpty() ?
                            "A " + module.getCategory() +" Module." + ChatFormatting.GREEN + " Name" + ChatFormatting.RESET + " «" +  module.getName() + "»."
                            : module.getDescription());

            drawDesc(renderer, mouseX + 8,mouseY + 8, description);
        }

        fontRenderer.drawString(module.getName(), getX() + 3.5f, (panel.isHovering(mouseX, mouseY, getX(), getY(), getWidth(), getHeight(false)) ? getY() + 1f : 2.5F + getY()), ExamplePlugin.theme.fontColor.getValue().getRGB());
    }
    public void drawDesc(IRenderer2D mesh2D, double x, double y, String text) {
        mesh2D.getMatrixStack().pushPose();
        mesh2D.getMatrixStack().translate(0F, 0F, 200F);
        IFontRenderer fontRenderer = RusherHackAPI.fonts().getFontRenderer();
        List<Tuple<Float, String>> pairs = new ArrayList<>();
        String[] lines = text.split("\n");
        float offset = 0;
        for (String s : lines) {
            pairs.add(new Tuple<>(offset, s));
            offset += (float) mesh2D.getFontRenderer().getFontHeight();
        }
        double maxWidth = Arrays.stream(lines)
                .map(fontRenderer::getStringWidth)
                .max(Comparator.comparing(i -> i)).orElse(0.0);
        double diff = Math.max(0, x + maxWidth - mc.getWindow().getGuiScaledWidth());
        double x0 = x - (diff + (diff > 0 ? 1F : 0F));
        mesh2D.drawRectangle(x0 - 0.5F, y - 0.5F, maxWidth + 0.5F, offset, new Color(0, 0, 0, 50).getRGB());

        for (Tuple<Float, String> pair : pairs) {
            fontRenderer.drawString(pair.getB(), x0, y + pair.getA() - 1F, ExamplePlugin.theme.fontColor.getValue().getRGB());
        }
        mesh2D.getMatrixStack().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(open) subItems.forEach(frame -> frame.mouseClicked(mouseX, mouseY, button));
        if (button == GLFW.GLFW_MOUSE_BUTTON_1 && panel.isHovering(mouseX, mouseY, getX(), getY(), getWidth(), getHeight(false))) {
            if(module instanceof ToggleableModule){
                ((ToggleableModule) module).toggle();
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_2 && panel.isHovering(mouseX, mouseY, getX(), getY(), getWidth(), getHeight(false))) {
            this.open = !this.open;
            possibleHeightUpdate();
        }
        return false;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if(open) subItems.forEach(frame -> frame.mouseReleased(mouseX, mouseY, button));
        IPanelItem.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char character) {
        if(open) subItems.forEach(frame -> frame.charTyped(character));
        return false;
    }

    @Override
    public boolean keyTyped(int key, int scanCode, int modifiers) {
        if(open) subItems.forEach(frame -> frame.keyTyped(key, scanCode, modifiers));
        return false;
    }
    protected void possibleHeightUpdate() {
        double temp = 13f;
        if (open)
            temp += subItems.stream().mapToDouble(i -> i.setting.isHidden() ? 0 : (i.getHeight(true) + 0.5f)).sum();
        rendererHeight = temp;
    }
    public void addSettingItems(List<Setting<?>> settings) {
        for(Setting<?> setting : settings) {
            if(setting instanceof BooleanSetting) {
                this.addSubItem(new BooleanItem(this.module, this.panel, this, (BooleanSetting) setting));
            }
            else if(setting instanceof StringSetting) {
                //string settings can either act like enum settings, or be configurable strings
                if (!((StringSetting) setting).getOptions().isEmpty()) {
                    this.addSubItem(new EnumItem(this, module, panel, setting));
                } else {
                    this.addSubItem(new StringItem(this, module, panel, setting));
                }
            }
            else if(setting instanceof EnumSetting<?>){
                this.addSubItem(new EnumItem(this, module, panel, setting));
            }
            else if(setting instanceof BindSetting){
                System.out.println("registered bindsetting for module " + module.getName());
                this.addSubItem(new BindItem(this, module, panel, setting));
            }
            else if(setting instanceof NumberSetting<?>){
                this.addSubItem(new NumberItem(this, module, panel, setting));
            }
//            } else if(setting instanceof ColorSetting) {
//                this.addSubItem(new ClassicColorItem(this.getPanel(), this, (ColorSetting) setting));
//            } else if(setting instanceof NullSetting) {
//                this.addSubItem(new ClassicSettingItem<>(this.getPanel(), this, (NullSetting) setting));
//            }
        }
    }
    public void addSubItem(ExtendableItem item) {
        this.subItems.add(item);
    }
}
