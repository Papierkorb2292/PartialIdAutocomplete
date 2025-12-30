package net.papierkorb2292.partial_id_autocomplete.client;

import com.mojang.serialization.Codec;
import joptsimple.util.RegexMatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.papierkorb2292.partial_id_autocomplete.client.mixin.OptionInstanceAccessor;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleOptionStringCallbacks implements OptionInstance.ValueSet<String> {

    public static final SimpleOptionStringCallbacks INSTANCE = new SimpleOptionStringCallbacks();

    private SimpleOptionStringCallbacks() {}

    @Override
    public Function<OptionInstance<String>, AbstractWidget> createButton(
            OptionInstance.TooltipSupplier<String> tooltipFactory,
            Options gameOptions,
            int x,
            int y,
            int width,
            Consumer<String> changeCallback
    ) {
        return option -> {
            final var textInput = new EditBox(
                    Minecraft.getInstance().font,
                    width / 2,
                    0,
                    width / 2,
                    20,
                    Component.literal("Regex input")
            );
            textInput.setMaxLength(128);
            textInput.setValue(option.get());
            final var label = new StringWidget(
                    0,
                    0,
                    width / 2 - 5,
                    20,
                    ((OptionInstanceAccessor)(Object)option).getCaption(),
                    Minecraft.getInstance().font
            );
            final var container = new AbstractContainerWidget(x, y, width, 20, Component.literal("")) {
                @Override
                protected int contentHeight() {
                    return Math.max(height, label.getHeight() + textInput.getHeight());
                }

                @Override
                protected double scrollRate() {
                    // Doesn't really matter atm, but this seems appropriate
                    return Minecraft.getInstance().font.lineHeight;
                }

                @Override
                public List<? extends GuiEventListener> children() {
                    return List.of(label, textInput);
                }

                @Override
                protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                    label.render(context, mouseX, mouseY, delta);
                    textInput.render(context, mouseX, mouseY, delta);
                }

                @Override
                protected void updateWidgetNarration(NarrationElementOutput builder) { }

                @Override
                public void setX(int x) {
                    final var deltaX = x - getX();
                    label.setX(deltaX + label.getX());
                    textInput.setX(deltaX + textInput.getX());
                    super.setX(x);
                }

                @Override
                public void setY(int y) {
                    final var deltaY = y - getY();
                    label.setY(deltaY + label.getY());
                    textInput.setY(deltaY + textInput.getY());
                    super.setY(y);
                }
            };
            container.setTooltip(tooltipFactory.apply(option.get()));
            textInput.setResponder(newText -> {
                option.set(newText);
                changeCallback.accept(newText);
                container.setTooltip(tooltipFactory.apply(newText));
            });
            return container;
        };
    }

    @Override
    public Optional<String> validateValue(String value) {
        return Optional.of(value);
    }

    @Override
    public Codec<String> codec() {
        return Codec.STRING;
    }
}
