package org.example.theme;

import lombok.Getter;
import lombok.Setter;
import org.example.theme.item.items.ModuleItem;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.ui.panel.IPanelItem;
import org.rusherhack.client.api.ui.panel.PanelBase;
import org.rusherhack.client.api.ui.panel.PanelHandlerBase;

import java.awt.*;
import java.util.List;

import static org.rusherhack.client.api.Globals.mc;


public class Panel extends PanelBase<IPanelItem> {
    public Panel(PanelHandlerBase handler, ModuleCategory category, double x, double y) {
        super(handler, category.getName());
        this.category = category;
        setX(x);
        setY(y);
    }

    @Setter
    @Getter
    private double scroll;
    @Getter
    private double modulesHeight;
    private double diffX;
    private double diffY;
    private double scrollHeight;
    private double scrollAmount;

    @Setter
    @Getter
    private double prevYModule;
    @Setter
    @Getter
    private double prevX;
    @Setter
    @Getter
    private double prevY;
    private double renderX;
    private double renderY;
    private double renderYModule;
    @Setter
    private boolean open = true, drag = false;
    @Getter
    private List<ModuleItem> moduleItems;
    private final ModuleCategory category;

    @Override
    public void render(RenderContext context, double mouseX, double mouseY) {
        System.out.println("Rendering panel " + this.getName() + " with " + moduleItems.size() + " items");

        if (drag) {
            setX(mouseX + diffX);
            setY(mouseY + diffY);
        }
        float diffScroll = (float) Math.max(-(getHeight() - scrollHeight), 0);

        if (getScroll() + scrollAmount >= -diffScroll && getScroll() + scrollHeight + scrollAmount <= getHeight() + diffScroll) {
            setScroll(getScroll() + scrollAmount);
        }

        if (getScroll() + scrollAmount < -diffScroll) {
            setScroll(-diffScroll);
        }

        if (getScroll() + scrollHeight + scrollAmount > getHeight() + diffScroll) {
            setScroll((float) (getHeight() + diffScroll - scrollHeight));
        }
        scrollAmount = 0;
        setRenderYModule(scroll);
        setRenderY(getY());
        setRenderX(getX());
        double x = getRenderX();
        double y = getRenderY();
        IRenderer2D renderer = RusherHackAPI.getRenderer2D();

        double height = this.getHeight();
        renderer.begin(context.pose());
        renderer.drawRectangle(x, y - 14.5f, getWidth(), height, Color.BLACK.getRGB());

        if (height > 0) {
            renderer.drawOutlinedRectangle(x, y, getWidth(), height + 1.5F, 1, ExamplePlugin.theme.colorSetting.getValue().getRGB(), Color.MAGENTA.getRGB());
        }
        double offsetX = (getWidth() - renderer.getFontRenderer().getStringWidth(category.toString())) / 2F;
        renderer.getFontRenderer().drawString(category.toString(), x + offsetX, y - 15 + 2.5F, ExamplePlugin.theme.fontColor.getValue().getRGB());
        renderer.scissorBox(x, y, x + getWidth(),y + height);
        double y0 = y + getRenderYModule() + 1.5F;
        renderer.begin(context.pose());
        if (height > 0) {
            for (ModuleItem frame : moduleItems) {
                frame.setX(x);
                frame.setY(y0);
                y0 += (frame.getHeight(false) + .5F);
                frame.render(context, mouseX, mouseY);
            }
            for (IPanelItem frame : getItemList()) {
                frame.render(context, mouseX, mouseY);
            }

            modulesHeight = y0;

        }

        renderer.end();
        renderer.endScissor();
    }

    @Override
    public double getWidth() {
        return 120;
    }

    @Override
    public double getHeight() {
        return 300;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (open) moduleItems.forEach(frame -> frame.mouseClicked(mouseX, mouseY, button));
        if (isHovering(mouseX, mouseY, getX(), getY() - 14.5F, getWidth(), 14.5F)) {
            if (button == 0) {
                ExamplePlugin.theme.getClickGuiHandler().getElements().forEach(element -> drag = false);
                diffX = getX() - mouseX;
                diffY = getY() - mouseY;
                drag = true;
            }
            if (button == 1) {
                open = !open;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollAmount = delta;
        return false;
    }
    public boolean isHovering(double mouseX, double mouseY, double x, double y, double width, double height) {
        return x < mouseX && width + x > mouseX && y < mouseY && height + y > mouseY;
    }
    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public boolean charTyped(char character) {
        if (open) moduleItems.forEach(frame -> frame.charTyped(character));
        return false;
    }

    @Override
    public boolean keyTyped(int key, int scanCode, int modifiers) {
        if (open) moduleItems.forEach(frame -> frame.keyTyped(key, scanCode, modifiers));
        return false;
    }
    public void setModuleItems(List<ModuleItem> moduleFrames) {
        this.moduleItems = moduleFrames;
        getItemList().addAll(moduleFrames);
        scrollHeight = (float) (getModuleItems().stream()
                .mapToDouble(frame -> frame.getHeight(false) + .5)
                .sum());
    }
    public void setRenderYModule(double y) {
        if (this.renderYModule == y) return;
        prevYModule = this.renderYModule;
        this.renderYModule = y;
    }

    public double getRenderYModule() {
        if (mc.getFps() < 20) {
            return renderYModule;
        }
        renderYModule = prevYModule + (renderYModule - prevYModule) * mc.getDeltaFrameTime() / (8 * (Math.min(240, mc.getFps()) / 240f));
        return renderYModule;
    }

    public void setRenderX(double x) {
        if (this.renderX == x) return;
        prevX = this.renderX;
        this.renderX = x;
    }

    public double getRenderX() {
        renderX = prevX + (renderX - prevX) * mc.getDeltaFrameTime() / (8 * (Math.min(240, mc.getFps()) / 240f));
        return renderX;
    }

    public void setRenderY(double y) {
        if (this.renderY == y) return;
        prevY = this.renderY;
        this.renderY = y;
    }


    public double getRenderY() {
        if (mc.getFps() < 20) {
            return renderY;
        }
        renderY = prevY + (renderY - prevY) * mc.getDeltaFrameTime() / (8 * (Math.min(240, mc.getFps()) / 240f));
        return renderY;
    }
}