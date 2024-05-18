package org.example.Engine.water;

import org.example.Engine.Display_Manager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class WaterFrameBuffers {

    static final int divisor1 = 2;
    static final int divisor2 = 1;

    protected static final int REFLECTION_WIDTH = 1280 / divisor1;
    private static final int REFLECTION_HEIGHT = 720 / divisor1;

    protected static final int REFRACTION_WIDTH = 1280 / divisor2;
    private static final int REFRACTION_HEIGHT = 720 / divisor2;

    private int reflectionFrameBuffer;
    private int reflectionTexture;
    private int reflectionDepthBuffer;

    private int refractionFrameBuffer;
    private int refractionTexture;
    private int refractionDepthTexture;

    /**
     * Initializes frame buffers for water rendering.
     * This should be called when loading the game.
     */
    public WaterFrameBuffers() {
        initializeReflectionFrameBuffer();
        initializeRefractionFrameBuffer();
    }

    /**
     * Cleans up the frame buffers and textures.
     * This should be called when closing the game.
     */
    public void cleanUp() {
        GL30.glDeleteFramebuffers(reflectionFrameBuffer);
        GL11.glDeleteTextures(reflectionTexture);
        GL30.glDeleteRenderbuffers(reflectionDepthBuffer);
        GL30.glDeleteFramebuffers(refractionFrameBuffer);
        GL11.glDeleteTextures(refractionTexture);
        GL11.glDeleteTextures(refractionDepthTexture);
    }

    /**
     * Binds the reflection frame buffer for rendering.
     * This should be called before rendering to this frame buffer.
     */
    public void bindReflectionFrameBuffer() {
        bindFrameBuffer(reflectionFrameBuffer, REFLECTION_WIDTH, REFLECTION_HEIGHT);
    }

    /**
     * Binds the refraction frame buffer for rendering.
     * This should be called before rendering to this frame buffer.
     */
    public void bindRefractionFrameBuffer() {
        bindFrameBuffer(refractionFrameBuffer, REFRACTION_WIDTH, REFRACTION_HEIGHT);
    }

    /**
     * Unbinds the current frame buffer and returns to the default frame buffer.
     * This should be called after rendering to a texture.
     */
    public void unbindCurrentFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display_Manager.getWidth(), Display_Manager.getHeight());
    }

    /**
     * Gets the resulting reflection texture.
     *
     * @return The texture ID of the reflection texture.
     */
    public int getReflectionTexture() {
        return reflectionTexture;
    }

    /**
     * Gets the resulting refraction texture.
     *
     * @return The texture ID of the refraction texture.
     */
    public int getRefractionTexture() {
        return refractionTexture;
    }

    /**
     * Gets the resulting refraction depth texture.
     *
     * @return The texture ID of the refraction depth texture.
     */
    public int getRefractionDepthTexture() {
        return refractionDepthTexture;
    }

    /**
     * Initializes the reflection frame buffer.
     */
    private void initializeReflectionFrameBuffer() {
        reflectionFrameBuffer = createFrameBuffer();
        reflectionTexture = createTextureAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        reflectionDepthBuffer = createDepthBufferAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    /**
     * Initializes the refraction frame buffer.
     */
    private void initializeRefractionFrameBuffer() {
        refractionFrameBuffer = createFrameBuffer();
        refractionTexture = createTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        refractionDepthTexture = createDepthTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    /**
     * Binds a frame buffer for rendering.
     *
     * @param frameBuffer The frame buffer ID to bind.
     * @param width The width of the frame buffer.
     * @param height The height of the frame buffer.
     */
    private void bindFrameBuffer(int frameBuffer, int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // To make sure the texture isn't bound
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        GL11.glViewport(0, 0, width, height);
    }

    /**
     * Creates a frame buffer and returns its ID.
     *
     * @return The frame buffer ID.
     */
    private int createFrameBuffer() {
        int frameBuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        return frameBuffer;
    }

    /**
     * Creates a texture attachment for a frame buffer.
     *
     * @param width The width of the texture.
     * @param height The height of the texture.
     * @return The texture ID.
     */
    private int createTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height,
                0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
        return texture;
    }

    /**
     * Creates a depth texture attachment for a frame buffer.
     *
     * @param width The width of the texture.
     * @param height The height of the texture.
     * @return The texture ID.
     */
    private int createDepthTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height,
                0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
        return texture;
    }

    /**
     * Creates a depth buffer attachment for a frame buffer.
     *
     * @param width The width of the buffer.
     * @param height The height of the buffer.
     * @return The buffer ID.
     */
    private int createDepthBufferAttachment(int width, int height) {
        int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        return depthBuffer;
    }
}
