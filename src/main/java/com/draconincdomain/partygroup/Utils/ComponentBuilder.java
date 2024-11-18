package com.draconincdomain.partygroup.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

/**
 * Custom Component Builder for Velocity API
 */
public class ComponentBuilder {
    private Component component;

    public ComponentBuilder(String text, TextColor color) {
        this.component = Component.text(text).color(color);
    }

    public static ComponentBuilder create(String text, TextColor color) {
        return new ComponentBuilder(text, color);
    }

    public ComponentBuilder append(String text, TextColor color) {
        this.component = component.append(Component.text(text).color(color));
        return this;
    }

    public ComponentBuilder hover(String hoverText) {
        return new ComponentBuilder(
                component.hoverEvent(HoverEvent.showText(Component.text(hoverText))),
                component.color()
        );
    }

    public ComponentBuilder click(String command) {
        return new ComponentBuilder(
                component.clickEvent(ClickEvent.runCommand(command)),
                component.color()
        );
    }

    public Component build() {
        return component;
    }

    private ComponentBuilder(Component component, TextColor color) {
        this.component = component.color(color);
    }
}
