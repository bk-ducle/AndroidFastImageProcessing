package project.android.imageprocessing.filter.colour;

import project.android.imageprocessing.filter.BasicFilter;
import android.opengl.GLES20;

public class HazeFilter extends BasicFilter {
	private static final String UNIFORM_DISTANCE = "u_Distance";
	private static final String UNIFORM_SLOPE = "u_Slope";
	
	private int distanceHandle;
	private int slopeHandle;
	private float distance;
	private float slope;
	
	public HazeFilter(float distance, float slope) {
		this.distance = distance;
		this.slope = slope;
	}
	
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		distanceHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_DISTANCE);
		slopeHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_SLOPE); 
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform1f(distanceHandle, distance);
		GLES20.glUniform1f(slopeHandle, slope);
	}
	
	@Override
	protected String getFragmentShader() {
		return 
				"precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform float "+UNIFORM_DISTANCE+";\n"	
				+"uniform float "+UNIFORM_SLOPE+";\n"	
				
		  		+"void main(){\n"
		  		+"   float d = "+VARYING_TEXCOORD+".y * "+UNIFORM_SLOPE+" + "+UNIFORM_DISTANCE+";\n"
		  		+"   vec4 color = vec4(d);\n"
		  		+"   vec4 c = texture2D("+UNIFORM_TEXTURE0+","+VARYING_TEXCOORD+");\n"
		  		+"   vec4 result = (c - color) / (1.0-d);\n"
		  		+"   gl_FragColor = vec4(result.rgb, c.a);\n"
		  		+"}\n";	
	}
}
