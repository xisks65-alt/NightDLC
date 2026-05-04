package dev.wh1tew1ndows.client.utils.render.shader;


import dev.wh1tew1ndows.client.api.interfaces.IMinecraft;
import dev.wh1tew1ndows.client.api.interfaces.IRender;
import dev.wh1tew1ndows.client.utils.file.FileUtils;
import dev.wh1tew1ndows.client.utils.render.shader.glsl.*;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL20;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Getter
public class ShaderManager implements IRender, IMinecraft {
    private final int programID;
    public static ShaderManager fontShader;
    public static ShaderManager kawaseUpBloom;
    public static ShaderManager kawaseDownBloom;
    public static ShaderManager entityChamsShader;
    public static ShaderManager entityOutlineShader;
    public static ShaderManager roundedShader;
    public static ShaderManager roundedTextureShader;
    public static ShaderManager roundedOutlineShader;
    public static ShaderManager mainmenuShader;
    public static ShaderManager blur;
    public static ShaderManager head;
    public static ShaderManager nois;
    public static ShaderManager legacyround;
    public static ShaderManager stencilShader;
    public static ShaderManager KAWASE_UP;
    public static ShaderManager KAWASE_DOWN;
    public static ShaderManager roundoni;


    public static void loadShaders() {
        fontShader = create(new FontGlsl());
        kawaseUpBloom = create(new KawaseUpBloomGlsl());
        kawaseDownBloom = create(new KawaseDownBloomGlsl());
        entityChamsShader = create(new EntityChamsGlsl());
        entityOutlineShader = create(new EntityOutlineGlsl());
        roundedShader = create(new RoundedGlsl());
        roundedTextureShader = create(new RoundedTextureGlsl());
        roundedOutlineShader = create(new RoundedOutline());
        mainmenuShader = create(new MainMenuGlsl());
        head = create(new HeadGlsl());
        roundoni = create(new RoundedOmniGlsl());
        nois = create(new noiseShader());
        blur = create(new BlurGlsl());
        legacyround = create(new LegacyRect());
        stencilShader = create(new StencilGlsl());
        KAWASE_UP = create(new KawaseBlurUp());
        KAWASE_DOWN = create(new KawaseBlurDown());
    }

    private ShaderManager(IShader fragmentShaderLoc, IShader vertexShaderLoc) {
        int program = glCreateProgram();
        int fragmentShaderID = createShader(new ByteArrayInputStream(fragmentShaderLoc.shader().getBytes()), GL_FRAGMENT_SHADER);
        GL20.glAttachShader(program, fragmentShaderID);
        int vertexShaderID = createShader(new ByteArrayInputStream(vertexShaderLoc.shader().getBytes()), GL_VERTEX_SHADER);
        GL20.glAttachShader(program, vertexShaderID);
        GL20.glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == 0) throw new IllegalStateException("Shader creation failed");
        this.programID = program;
    }

    public static ShaderManager create(IShader shader) {
        return new ShaderManager(shader, new VertexGlsl());
    }

    public static ShaderManager create(IShader fragShader, IShader vertexShader) {
        return new ShaderManager(fragShader, vertexShader);
    }


    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, FileUtils.readInputStream(inputStream));
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.out.println(glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile", shaderType));
        }
        return shader;
    }

    public void load() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    public int getUniform(String name) {
        return glGetUniformLocation(programID, name);
    }

    public ShaderManager setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1f(loc, args[0]);
            case 2 -> glUniform2f(loc, args[0], args[1]);
            case 3 -> glUniform3f(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4f(loc, args[0], args[1], args[2], args[3]);
        }
        return this;
    }


    public ShaderManager setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1i(loc, args[0]);
            case 2 -> glUniform2i(loc, args[0], args[1]);
            case 3 -> glUniform3i(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4i(loc, args[0], args[1], args[2], args[3]);
        }
        return this;
    }

    public void setUniform(String name, float... args) {
        int loc = ARBShaderObjects.glGetUniformLocationARB(programID, name);
        switch (args.length) {
            case 1 -> ARBShaderObjects.glUniform1fARB(loc, args[0]);
            case 2 -> ARBShaderObjects.glUniform2fARB(loc, args[0], args[1]);
            case 3 -> ARBShaderObjects.glUniform3fARB(loc, args[0], args[1], args[2]);
            case 4 -> ARBShaderObjects.glUniform4fARB(loc, args[0], args[1], args[2], args[3]);
            default ->
                    throw new IllegalArgumentException("Недопустимое количество аргументов для uniform '" + name + "'");
        }
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.getMainWindow().getWidth()
                || framebuffer.framebufferHeight != mc.getMainWindow().getHeight();
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        try {
            if (needsNewFramebuffer(framebuffer)) {
                if (framebuffer != null) {
                    framebuffer.deleteFramebuffer();
                }
                return new Framebuffer(mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight(), depth, false);
            }
            return framebuffer;
        } catch (Exception ex) {
            return null;
        }
    }

    public ShaderManager setMat4fv(String name, FloatBuffer matrix) {
        int loc = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(loc, false, matrix);
        return this;
    }

    public ShaderManager setMat4fv(String name, float[] matrix) {
        int loc = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(loc, false, matrix);
        return this;
    }

    public static void drawQuads(float x, float y, float width, float height) {
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f(x, y);
        glTexCoord2f(0, 1);
        glVertex2f(x, y + height);
        glTexCoord2f(1, 1);
        glVertex2f(x + width, y + height);
        glTexCoord2f(1, 0);
        glVertex2f(x + width, y);
        glEnd();
    }

    public static void drawQuads() {
        MainWindow sr = mc.getMainWindow();
        float width = (float) sr.getScaledWidth();
        float height = (float) sr.getScaledHeight();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(0, 0);
        glVertex2f(0, height);
        glTexCoord2f(1, 0);
        glVertex2f(width, height);
        glTexCoord2f(1, 1);
        glVertex2f(width, 0);
        glEnd();
    }

    public ShaderManager setMat4fv(String name, MatrixStack matrix) {
        setMat4fv(name, matrix.getLast().getMatrix());
        return this;
    }

    public ShaderManager setMat4fv(String name, Matrix4f matrix) {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
        matrix.write(floatBuffer);
        setMat4fv(name, floatBuffer);
        return this;
    }
}