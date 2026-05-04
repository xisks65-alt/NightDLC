package dev.wh1tew1ndows.client.utils.render.shader.glsl;

import dev.wh1tew1ndows.client.utils.render.shader.IShader;

public class RoundedOmniGlsl implements IShader {
    @Override
    public String shader() {
        return """
                #version 120
                
                uniform sampler2D image;
                uniform vec4 color1;
                uniform vec4 color2;
                uniform vec4 color3;
                uniform vec4 color4;
                
                uniform vec2 size;
                uniform vec4 round;
                uniform float value;
                uniform vec2 smoothness;
                uniform float thickness;
                uniform vec2 resolution;
                uniform int type; // 0 - rect, 1 - shadow, 2 - texture, 3 - blur, 4 - outline
                uniform float alpha;
                
                float roundedBox(vec2 pos, vec2 b, vec4 r) {
                     vec2 signPos = step(vec2(0.0), pos);
                     vec2 radius = mix(r.zw, r.xy, signPos.x);
                     float rVal = radius.y * (1.0 - signPos.y) + radius.x * signPos.y;
                
                     vec2 q = abs(pos) - b + rVal;
                
                     float qMax = (q.x > q.y) ? q.x : q.y;
                
                     vec2 qClamped = max(q, vec2(0.0));
                     float qLength = sqrt(dot(qClamped, qClamped));
                
                     return min(qMax, 0.0) + qLength - rVal;
                }
                
                vec4 createGradient(vec2 uv) {
                    vec4 topColor = color1 + (color3 - color1) * uv.x;
                    vec4 bottomColor = color2 + (color4 - color2) * uv.x;
                    return topColor + (bottomColor - topColor) * uv.y;
                }
                
                void main() {
                    vec2 uv = gl_TexCoord[0].st;
                    vec2 halfSize = size * 0.5;
                    vec2 pos = uv * size - halfSize;
                
                    float d = roundedBox(pos, halfSize - value, round);
                    float sAlpha = (1.0 - smoothstep(smoothness.x, smoothness.y, d)) * alpha;
                
                    vec4 grad;
                    if (type == 0 || type == 1 || type == 4) {
                        grad = createGradient(uv);
                    }
                
                    if (type == 0) {
                        gl_FragColor = vec4(grad.rgb, grad.a * sAlpha);
                    } else if (type == 1) {
                        gl_FragColor = vec4(grad.rgb, grad.a * sAlpha * sAlpha);
                    } else if (type == 2) {
                        vec4 texColor = texture2D(image, uv);
                        gl_FragColor = vec4(texColor.rgb, texColor.a * sAlpha);
                    } else if (type == 3) {
                        vec4 blurColor = texture2D(image, gl_FragCoord.xy / resolution);
                        gl_FragColor = vec4(blurColor.rgb, blurColor.a * sAlpha * sAlpha);
                    } else if (type == 4) {
                        float dOutline = roundedBox(pos, halfSize - value - thickness + smoothness.x, round);
                        float bAlpha = (1.0 - smoothstep(smoothness.x + thickness, smoothness.y + thickness, abs(dOutline))) * alpha;
                        gl_FragColor = vec4(grad.rgb, grad.a * sAlpha * bAlpha);
                    } else {
                        gl_FragColor = vec4(0.0);
                    }
                }""";
    }
}
