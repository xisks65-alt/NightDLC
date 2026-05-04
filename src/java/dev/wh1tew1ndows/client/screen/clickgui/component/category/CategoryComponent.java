package dev.wh1tew1ndows.client.screen.clickgui.component.category;

import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.module.Category;
import dev.wh1tew1ndows.client.managers.module.Module;
import dev.wh1tew1ndows.client.screen.clickgui.ClickGuiScreen;
import dev.wh1tew1ndows.client.screen.clickgui.component.WindowComponent;
import dev.wh1tew1ndows.client.screen.clickgui.component.module.ModuleComponent;
import dev.wh1tew1ndows.client.utils.render.font.Fonts;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class CategoryComponent extends WindowComponent {
    private final Category category;
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private float moduleHeight = 0;
    private final float margin = 5;

    public CategoryComponent(Category category, ClickGuiScreen clickGui) {
        this.category = category;
        this.moduleComponents.addAll(getModules().stream().map(module -> new ModuleComponent(module, clickGui)).toList());
        size.set(clickGui.categoryWidth(), clickGui.categoryHeight());
    }

    private List<Module> getModules() {
        List<Module> modulesList = Zetrix.inst().moduleManager().get(category);
        modulesList.sort(Comparator.comparing(module -> -Fonts.MONTSERRAT_MEDIUM.getWidth(module.getModuleInfo().name(),7)));
        return modulesList;
    }


    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        moduleComponents.forEach(component -> component.resize(minecraft, width, height));
    }

    @Override
    public void init() {
        moduleComponents.forEach(ModuleComponent::init);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        float colW = size.x;
        float gap = 6f;
        float col1X = position.x;
        float col2X = position.x + colW + gap;

        List<ModuleComponent> visible = moduleComponents.stream()
                .filter(m -> !Zetrix.inst().clickGui().searchCheck(m.getModule().getName()))
                .toList();

        List<ModuleComponent> col1 = new ArrayList<>();
        List<ModuleComponent> col2 = new ArrayList<>();
        for (int i = 0; i < visible.size(); i++) {
            if (i % 2 == 0) col1.add(visible.get(i));
            else col2.add(visible.get(i));
        }

        float off1 = position.y;
        float off2 = position.y;
        float maxH = 0;

        for (ModuleComponent comp : col1) {
            comp.position().set(col1X, off1);
            comp.size().x = colW;
            comp.render(matrix, mouseX, mouseY, partialTicks);
            float h = comp.size().y + (float)(comp.expandAnimation().getValue() * comp.getSettingHeight());
            off1 += h + gap;
            if (off1 - position.y > maxH) maxH = off1 - position.y;
        }

        for (ModuleComponent comp : col2) {
            comp.position().set(col2X, off2);
            comp.size().x = colW;
            comp.render(matrix, mouseX, mouseY, partialTicks);
            float h = comp.size().y + (float)(comp.expandAnimation().getValue() * comp.getSettingHeight());
            off2 += h + gap;
            if (off2 - position.y > maxH) maxH = off2 - position.y;
        }

        moduleHeight = maxH;
    }

    /** Устанавливает ширину одной колонки модулей */
    public void setColumnWidth(float w) {
        size.x = w;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ModuleComponent component : moduleComponents) {
            if (Zetrix.inst().clickGui().searchCheck(component.getModule().getName())) continue;
            component.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ModuleComponent component : moduleComponents) {
            if (Zetrix.inst().clickGui().searchCheck(component.getModule().getName())) continue;
            component.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ModuleComponent component : moduleComponents) {
            if (Zetrix.inst().clickGui().searchCheck(component.getModule().getName())) continue;
            component.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (ModuleComponent component : moduleComponents) {
            if (Zetrix.inst().clickGui().searchCheck(component.getModule().getName())) continue;
            component.keyReleased(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : moduleComponents) {
            if (Zetrix.inst().clickGui().searchCheck(component.getModule().getName())) continue;
            component.charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public void onClose() {
        moduleComponents.forEach(ModuleComponent::onClose);
    }
}