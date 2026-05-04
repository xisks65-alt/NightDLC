package net.minecraft.client.gui;

import dev.wh1tew1ndows.client.utils.animation.compactanimation.CompactAnimation;
import dev.wh1tew1ndows.client.utils.animation.compactanimation.Easing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatLine<T> {


    @Getter
    private final int updateCounterCreated;
    private T lineString;
    private final int chatLineID;
    @Getter
    private boolean isClient;
    private CompactAnimation slideAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 450L);

    public ChatLine(int updatedCounterCreated, T lineString, int chatLineID, boolean isClient) {
        this.lineString = lineString;
        this.updateCounterCreated = updatedCounterCreated;
        this.chatLineID = chatLineID;
        this.isClient = isClient;
    }

    public T getLineString() {
        return this.lineString;
    }

    public int getUpdatedCounter() {
        return this.updateCounterCreated;
    }

    public int getChatLineID() {
        return this.chatLineID;
    }

    public CompactAnimation getSlideAnimation() {
        return this.slideAnimation;
    }

}
