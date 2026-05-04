package net.minecraft.util;

import dev.wh1tew1ndows.client.api.client.Constants;

public class Namespaced extends ResourceLocation {
    public Namespaced(String location) {
        super(Constants.NAMESPACE + "/" + location);
    }
}
