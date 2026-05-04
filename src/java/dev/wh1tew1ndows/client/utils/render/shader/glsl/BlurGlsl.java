package dev.wh1tew1ndows.client.utils.render.shader.glsl;

import dev.wh1tew1ndows.client.utils.render.shader.IShader;

public class BlurGlsl implements IShader {

    @Override
    public String shader() {
        return "#version 120\n\nuniform sampler2D textureIn;\nuniform sampler2D textureOut;\nuniform vec2 texelSize, direction;\nuniform float radius, weights[256];\n\n#define offset texelSize * direction\n\nvoid main() {\n    vec2 uv = gl_TexCoord[0].st;\n    uv.y = 1.0f - uv.y;\n\n    float alpha = texture2D(textureOut, uv).a;\n    if (direction.x == 0.0 && alpha == 0.0) {\n        discard;\n    }\n\n    vec3 color = texture2D(textureIn, gl_TexCoord[0].st).rgb * weights[0];\n    float totalWeight = weights[0];\n\n    for (float f = 1.0; f <= radius; f++) {\n        color += texture2D(textureIn, gl_TexCoord[0].st + f * offset).rgb * (weights[int(abs(f))]);\n        color += texture2D(textureIn, gl_TexCoord[0].st - f * offset).rgb * (weights[int(abs(f))]);\n\n        totalWeight += (weights[int(abs(f))]) * 2.0;\n    }\n\n    gl_FragColor = vec4(color / totalWeight, 1.0);\n}\n";
    }
}

