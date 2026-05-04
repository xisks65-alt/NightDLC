package dev.wh1tew1ndows.client.utils.render.shader.glsl;

import dev.wh1tew1ndows.client.utils.render.shader.IShader;

public class noiseShader implements IShader {
    @Override
    public String shader() {
        return """
                uniform sampler2D u_texture;
                uniform float u_value;
                #define NOISE .5/255.0
                
                float random(vec2 st) {
                    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453);
                }
                
                void main() {
                    vec2 st = gl_TexCoord[0].st;
                
                    // Получение цвета из входной текстуры
                    vec4 color = texture2D(u_texture, st);
                
                    float noise = (sin(st.x) * cos(st.y)) * random(st);
                
                    // Применение шума
                    color.rgb -= vec3(noise / u_value);
                    // Отрисовка на выход
                    gl_FragColor = color;
                }
                """;
    }
}
