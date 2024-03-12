package engine.graphics;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import engine.maths.Matrix4f;
import engine.maths.Vector2f;
import engine.maths.Vector3f;
import engine.utils.FileUtils;

public class Shader {
	private final String vertexSource =
			"#version 330 core\n" +
					"layout (location = 0) in vec3 aPos;\n" +
					"uniform mat4 projection;\n" +
					"uniform mat4 view;\n" +
					"uniform mat4 model;\n" +
					"void main()\n" +
					"{\n" +
					"    gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
					"}\n";

	private final String fragmentSource =
			"#version 330 core\n" +
					"out vec4 FragColor;\n" +
					"void main()\n" +
					"{\n" +
					"    FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n" +
					"}\n";

	private int vertexID, fragmentID, programID;

	public Shader() {
	}

	public void create() {
		programID = GL20.glCreateProgram();
		vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

		GL20.glShaderSource(vertexID, vertexSource);
		GL20.glCompileShader(vertexID);

		if (GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Vertex Shader: " + GL20.glGetShaderInfoLog(vertexID));
			return;
		}

		fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

		GL20.glShaderSource(fragmentID, fragmentSource);
		GL20.glCompileShader(fragmentID);

		if (GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Fragment Shader: " + GL20.glGetShaderInfoLog(fragmentID));
			return;
		}

		GL20.glAttachShader(programID, vertexID);
		GL20.glAttachShader(programID, fragmentID);

		GL20.glLinkProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println("Program Linking: " + GL20.glGetProgramInfoLog(programID));
			return;
		}

		GL20.glValidateProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Program Validation: " + GL20.glGetProgramInfoLog(programID));
			return;
		}
	}

	public int getUniformLocation(String name) {
		return GL20.glGetUniformLocation(programID, name);
	}
	
	public void setUniform(String name, float value) {
		GL20.glUniform1f(getUniformLocation(name), value);
	}
	
	public void setUniform(String name, int value) {
		GL20.glUniform1i(getUniformLocation(name), value);
	}
	
	public void setUniform(String name, boolean value) {
		GL20.glUniform1i(getUniformLocation(name), value ? 1 : 0);
	}
	
	public void setUniform(String name, Vector2f value) {
		GL20.glUniform2f(getUniformLocation(name), value.getX(), value.getY());
	}
	
	public void setUniform(String name, Vector3f value) {
		GL20.glUniform3f(getUniformLocation(name), value.getX(), value.getY(), value.getZ());
	}
	
	public void setUniform(String name, Matrix4f value) {
		FloatBuffer matrix = MemoryUtil.memAllocFloat(Matrix4f.SIZE * Matrix4f.SIZE);
		matrix.put(value.getAll()).flip();
		GL20.glUniformMatrix4fv(getUniformLocation(name), true, matrix);
	}
	
	public void bind() {
		GL20.glUseProgram(programID);
	}
	
	public void unbind() {
		GL20.glUseProgram(0);
	}
	
	public void destroy() {
		GL20.glDetachShader(programID, vertexID);
		GL20.glDetachShader(programID, fragmentID);
		GL20.glDeleteShader(vertexID);
		GL20.glDeleteShader(fragmentID);
		GL20.glDeleteProgram(programID);
	}
}