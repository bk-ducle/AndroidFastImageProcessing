package project.android.imageprocessing.filter.processing;

import project.android.imageprocessing.filter.TwoPassMultiPixelFilter;
import android.opengl.GLES20;

public class SingleComponentGaussianBlurFilter extends TwoPassMultiPixelFilter {
	private static final String UNIFORM_BLUR_SIZE = "u_BlurSize";
	
	private int blurSizeHandle;
	private float blurSize;
	
	public SingleComponentGaussianBlurFilter(float blurSize) {
		this.blurSize = blurSize;
	}
	
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		blurSizeHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_BLUR_SIZE);
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform1f(blurSizeHandle, blurSize);
	} 
	
	@Override
	protected String getFragmentShader() {
		return 
				 "precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform float "+UNIFORM_BLUR_SIZE+";\n"
				+"uniform float "+UNIFORM_TEXELWIDTH+";\n"
				+"uniform float "+UNIFORM_TEXELHEIGHT+";\n"
						
				
		  		+"void main(){\n"
				+"   vec2 singleStepOffset = vec2("+UNIFORM_TEXELWIDTH+", "+UNIFORM_TEXELHEIGHT+");\n"
				+"   int multiplier = 0;\n"
				+"   vec2 blurStep = vec2(0,0);\n"
				+"   vec2 blurCoordinates[9];"
				+"   for(int i = 0; i < 9; i++) {\n"
				+"     multiplier = (i - 4);\n"
				+"     blurStep = float(multiplier) * singleStepOffset;\n"
				+"     blurCoordinates[i] = "+VARYING_TEXCOORD+".xy + blurStep;\n"
				+"   }\n"
				+"   float sum = 0.0;\n"
				+"   vec4 color = texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[4]);\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[0]).r * 0.05;\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[1]).r * 0.09;\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[2]).r * 0.12;\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[3]).r * 0.15;\n"
				+"   sum += color.r * 0.18;\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[5]).r * 0.15;\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[6]).r * 0.12;\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[7]).r * 0.09;\n"
				+"   sum += texture2D("+UNIFORM_TEXTURE0+", blurCoordinates[8]).r * 0.05;\n"
		  		+"   gl_FragColor = vec4(vec3(sum), color.a);\n"
		  		+"}\n";
	}
}
