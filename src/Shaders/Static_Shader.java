package Shaders;

public class Static_Shader extends Shader_Program {

    public static final String VERTEX_FILE = "C:\\Users\\yuval\\Documents\\GitHub\\3D_Engine\\src\\Shaders\\vertex_shader.glsl";
    public static final String FRAGMENT_FILE = "C:\\Users\\yuval\\Documents\\GitHub\\3D_Engine\\src\\Shaders\\fragment_shader.glsl";

    public Static_Shader(){
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttributes(0, "position");
    }
}
