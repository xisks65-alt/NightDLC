package dev.wh1tew1ndows.client.utils.render.shader.impl.util;

import dev.wh1tew1ndows.client.utils.render.shader.IShader;

public class KawaseBlurUp implements IShader {

    @Override
    public String shader() {
        return """
                #version 120
                
                uniform sampler2D inTexture, textureToCheck;
                uniform vec2 halfpixel, offset, iResolution;
                uniform float saturation;
                uniform int check;
                
                void main() {
                    vec2 uv = vec2(gl_FragCoord.xy / iResolution);
                    vec4 sum = texture2D(inTexture, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset);
                    sum.rgb *= saturation;
                    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;
                    sum += texture2D(inTexture, uv + vec2(0.0, halfpixel.y * 2.0) * offset);
                    sum += texture2D(inTexture, uv + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;
                    sum += texture2D(inTexture, uv + vec2(halfpixel.x * 2.0, 0.0) * offset);
                    sum += texture2D(inTexture, uv + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;
                    sum += texture2D(inTexture, uv + vec2(0.0, -halfpixel.y * 2.0) * offset);
                    sum += texture2D(inTexture, uv + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;
                
                    gl_FragColor = vec4(sum.rgb / 10.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));
                }
                """;

    }
}
