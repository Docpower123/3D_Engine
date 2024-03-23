package Shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public abstract class Shader_Program {

    private int ProgramId;
    private int vertexShaderID;
    private int fragmnetShaderID;

    public Shader_Program(String vertexFile, String fragmentFile){
        vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        fragmnetShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        ProgramId = GL20.glCreateProgram();
        GL20.glAttachShader(ProgramId, vertexShaderID);
        GL20.glAttachShader(ProgramId, fragmnetShaderID);
        GL20.glLinkProgram(ProgramId);
        GL20.glValidateProgram(ProgramId);
    }

    public void start(){
        GL20.glUseProgram(ProgramId);
    }

    public void stop(){
        GL20.glUseProgram(0);
    }

    public void cleanUp(){
        stop();
        GL20.glDetachShader(ProgramId, vertexShaderID);
        GL20.glDetachShader(ProgramId, fragmnetShaderID);
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmnetShaderID);
        GL20.glDeleteProgram(ProgramId);
    }

    protected abstract void bindAttributes();

    protected void bindAttributes(int attribute, String varibleName){
        GL20.glBindAttribLocation(ProgramId, attribute, varibleName);
    }

    private static int loadShader(String file, int type){
        StringBuilder shaderSource = new StringBuilder();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!=null){
                shaderSource.append(line).append("//\n");
            }
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS )== GL11.GL_FALSE){
            System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }
        return shaderID;
    }

    }

