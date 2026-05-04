package dev.wh1tew1ndows.client.managers.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    COMBAT("Combat", "u"),
    MOVEMENT("Movement", "k"),
    RENDER("Render", "s"),
    PLAYER("Player", "b"),
    MISC("Misc", "v");
    private final String name;
    private final String icon;
}