package net.papierkorb2292.partial_id_autocomplete.client;

import com.mojang.serialization.Codec;
import joptsimple.util.RegexMatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.papierkorb2292.partial_id_autocomplete.client.mixin.SimpleOptionAccessor;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleOptionStringCallbacks implements SimpleOption.Callbacks<String> {

    public static final SimpleOptionStringCallbacks INSTANCE = new SimpleOptionStringCallbacks();

    private SimpleOptionStringCallbacks() {}

    @Override
    public Function<SimpleOption<String>, ClickableWidget> getWidgetCreator(
            SimpleOption.TooltipFactory<String> tooltipFactory,
            GameOptions gameOptions,
            int x,
            int y,
            int width,
            Consumer<String> changeCallback
    ) {
        return option -> {
            final var textInput = new TextFieldWidget(
                    MinecraftClient.getInstance().textRenderer,
                    width / 2,
                    0,
                    width / 2,
                    20,
                    Text.literal("Regex input")
            );
            textInput.setText(option.getValue());
            final var label = new TextWidget(
                    0,
                    0,
                    width / 2 - 5,
                    20,
                    ((SimpleOptionAccessor)(Object)option).getText(),
                    MinecraftClient.getInstance().textRenderer
            );
            final var container = new ContainerWidget(x, y, width, 20, Text.literal("")) {
                @Override
                public List<? extends Element> children() {
                    return List.of(label, textInput);
                }

                @Override
                protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    label.render(context, mouseX, mouseY, delta);
                    textInput.render(context, mouseX, mouseY, delta);
                }

                @Override
                protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

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
            container.setTooltip(tooltipFactory.apply(option.getValue()));
            textInput.setChangedListener(newText -> {
                option.setValue(newText);
                changeCallback.accept(newText);
                container.setTooltip(tooltipFactory.apply(newText));
            });
            return container;
        };
    }

    @Override
    public Optional<String> validate(String value) {
        return Optional.of(value);
    }

    @Override
    public Codec<String> codec() {
        return Codec.STRING;
    }
}
