package renderEngine;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class Display_Manager {

   private static final int WIDTH = 1280;
   private static final int HEIGHT = 720;
   private static final String TITLE = "OpenGL Window";

   private long window;

   public void createDisplay() {
      // Initialize GLFW
      if (!GLFW.glfwInit()) {
         throw new IllegalStateException("Unable to initialize GLFW");
      }

      // Configure GLFW
      GLFW.glfwDefaultWindowHints();
      GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
      GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

      // Create the window
      window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
      if (window == MemoryUtil.NULL) {
         throw new RuntimeException("Failed to create the GLFW window");
      }

      // Make the OpenGL context current
      GLFW.glfwMakeContextCurrent(window);

      // Enable v-sync
      GLFW.glfwSwapInterval(1);

      // Make the window visible
      GLFW.glfwShowWindow(window);

      // Initialize OpenGL
      GL.createCapabilities();
      GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
   }

   public void updateDisplay() {
      // Swap buffers
      GLFW.glfwSwapBuffers(window);

      // Poll for window events
      GLFW.glfwPollEvents();
   }

   public void closeDisplay() {
      // Destroy the window
      GLFW.glfwDestroyWindow(window);

      // Terminate GLFW
      GLFW.glfwTerminate();
   }

   public boolean shouldClose() {
      return GLFW.glfwWindowShouldClose(window);
   }
}
