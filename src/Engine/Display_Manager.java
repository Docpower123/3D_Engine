package Engine;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;

public class Display_Manager {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int FPS_CAP = 120;

    private static final String TITLE = "Game";
    public static void create() {
        // attributes of the display
        ContextAttribs attribs = new ContextAttribs(3,2);
        attribs.withForwardCompatible(true);
        attribs.withProfileCore(true);

        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.create(new PixelFormat(), attribs);
            Display.setTitle(TITLE);

        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        // where in the display the game be rendered at
        GL11.glViewport(0, 0, WIDTH, HEIGHT);
    }
    public static void update(){
        Display.sync(FPS_CAP);
        Display.update();
    }

    public static void close() {
        Display.destroy();
    }

}
