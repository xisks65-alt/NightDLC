package dev.wh1tew1ndows.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import dev.wh1tew1ndows.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class ChunkRenderEvent extends Event {
    private final ChunkRenderDispatcher.ChunkRender chunkRender;
}
