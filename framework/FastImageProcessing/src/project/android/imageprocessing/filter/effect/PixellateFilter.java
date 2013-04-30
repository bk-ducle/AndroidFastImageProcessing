package project.android.imageprocessing.filter.effect;

import project.android.imageprocessing.filter.BasicFilter;
import android.opengl.GLES20;

public class PixellateFilter extends BasicFilter {
	protected static final String UNIFORM_FRACTIONAL_WIDTH = "u_FractionalWidth";
	protected static final String UNIFORM_ASPECT_RATIO = "u_AspectRatio";
	
	private int fractionalWidthHandle;
	private int aspectRatioHandle;
	private float fractionalWidth;
	private float aspectRatio;
	
	public PixellateFilter(float fractionalWidth, float aspectRatio) {
		this.fractionalWidth = fractionalWidth;
		this.aspectRatio = aspectRatio;
	}
	
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		fractionalWidthHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_FRACTIONAL_WIDTH);
		aspectRatioHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_ASPECT_RATIO);
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform1f(fractionalWidthHandle, fractionalWidth);
		GLES20.glUniform1f(aspectRatioHandle, aspectRatio);
	}
	
	@Override
	protected String getFragmentShader() {
		return 
				"precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform float "+UNIFORM_FRACTIONAL_WIDTH+";\n"	
				+"uniform float "+UNIFORM_ASPECT_RATIO+";\n"
				
		  		+"void main(){\n"
		  		+"   highp vec2 sampleDivisor = vec2("+UNIFORM_FRACTIONAL_WIDTH+", "+UNIFORM_FRACTIONAL_WIDTH+" / "+UNIFORM_ASPECT_RATIO+");\n"
			    +"   highp vec2 samplePos = "+VARYING_TEXCOORD+" - mod("+VARYING_TEXCOORD+", sampleDivisor) + 0.5 * sampleDivisor;\n"
		  		+"   gl_FragColor = texture2D("+UNIFORM_TEXTURE0+", samplePos);\n"
		  		+"}\n";	
	}
}
