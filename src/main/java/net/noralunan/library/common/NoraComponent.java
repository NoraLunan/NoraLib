package net.noralunan.library.common;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NoraComponent implements Component {
    private MutableComponent component;

    private NoraComponent(String text) {
        this.component = Component.literal(text);
    }

    private NoraComponent(MutableComponent component) {
        this.component = component;
    }

    private NoraComponent() {
        this.component = Component.empty();
    }

    @Override
    public @NotNull Style getStyle() {
        return this.component.getStyle();
    }

    @Override
    public @NotNull ComponentContents getContents() {
        return this.component.getContents();
    }

    @Override
    public @NotNull String getString() {
        return this.component.getString();
    }

    @Override
    public @NotNull List<Component> getSiblings() {
        return this.component.getSiblings();
    }

    @Override
    public @NotNull FormattedCharSequence getVisualOrderText() {
        return this.component.getVisualOrderText();
    }

    public NoraComponent darkRed() {
        this.component = this.component.withStyle(ChatFormatting.DARK_RED);
        return this;
    }

    public NoraComponent red() {
        this.component = this.component.withStyle(ChatFormatting.RED);
        return this;
    }

    public NoraComponent gold() {
        this.component = this.component.withStyle(ChatFormatting.GOLD);
        return this;
    }

    public NoraComponent yellow() {
        this.component = this.component.withStyle(ChatFormatting.YELLOW);
        return this;
    }

    public NoraComponent darkGreen() {
        this.component = this.component.withStyle(ChatFormatting.DARK_GREEN);
        return this;
    }

    public NoraComponent green() {
        this.component = this.component.withStyle(ChatFormatting.GREEN);
        return this;
    }

    public NoraComponent aqua() {
        this.component = this.component.withStyle(ChatFormatting.AQUA);
        return this;
    }

    public NoraComponent darkAqua() {
        this.component = this.component.withStyle(ChatFormatting.DARK_AQUA);
        return this;
    }

    public NoraComponent darkBlue() {
        this.component = this.component.withStyle(ChatFormatting.DARK_BLUE);
        return this;
    }

    public NoraComponent blue() {
        this.component = this.component.withStyle(ChatFormatting.BLUE);
        return this;
    }

    public NoraComponent purple() {
        this.component = this.component.withStyle(ChatFormatting.LIGHT_PURPLE);
        return this;
    }

    public NoraComponent darkPurple() {
        this.component = this.component.withStyle(ChatFormatting.DARK_PURPLE);
        return this;
    }

    public NoraComponent white() {
        this.component = this.component.withStyle(ChatFormatting.WHITE);
        return this;
    }

    public NoraComponent gray() {
        this.component = this.component.withStyle(ChatFormatting.GRAY);
        return this;
    }

    public NoraComponent darkGray() {
        this.component = this.component.withStyle(ChatFormatting.DARK_GRAY);
        return this;
    }

    public NoraComponent black() {
        this.component = this.component.withStyle(ChatFormatting.BLACK);
        return this;
    }

    public NoraComponent bold() {
        this.component = this.component.withStyle(Style.EMPTY.withBold(true));
        return this;
    }

    public NoraComponent italic() {
        this.component = this.component.withStyle(Style.EMPTY.withItalic(true));
        return this;
    }

    public NoraComponent color(int rgb) {
        this.component = this.component.withStyle(Style.EMPTY.withColor(rgb));
        return this;
    }

    public NoraComponent color(TextColor color) {
        this.component = this.component.withStyle(Style.EMPTY.withColor(color));
        return this;
    }

    public NoraComponent color(ChatFormatting format) {
        this.component = this.component.withStyle(format);
        return this;
    }

    public NoraComponent append(Component component) {
        this.component = this.component.append(component);
        return this;
    }

    public NoraComponent insertion(String text) {
        this.component = this.component.withStyle(Style.EMPTY.withInsertion(text));
        return this;
    }

    public static NoraComponent line() {
        return new NoraComponent();
    }

    public static NoraComponent line(String text) {
        return new NoraComponent(text);
    }

    public static Component of(@Nullable String text) {
        return Component.nullToEmpty(text);
    }

    public static MutableComponent of(String text, ChatFormatting format) {
        return Component.literal(text).withStyle(format);
    }

    public static MutableComponent of(String text, Style style) {
        return Component.literal(text).withStyle(style);
    }

}
