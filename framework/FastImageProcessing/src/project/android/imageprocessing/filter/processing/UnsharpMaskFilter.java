package project.android.imageprocessing.filter.processing;

import project.android.imageprocessing.filter.CompositeFilter;
import project.android.imageprocessing.input.GLTextureOutputRenderer;
import android.opengl.GLES20;

public class UnsharpMaskFilter extends CompositeFilter {
	private static final String UNIFORM_INTENSITY = "u_Intensity";

	private float intensity;
	private int intensityHandle;
	
	private GaussianBlurFilter blur;
	
	public UnsharpMaskFilter(float blurSize, float intensity) {
		super(2);
		this.intensity = intensity;
		
		blur = new GaussianBlurFilter(blurSize);
		blur.addTarget(this);

		registerInitialFilter(blur);
		registerTerminalFilter(blur);
	}
		
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		intensityHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_INTENSITY);
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform1f(intensityHandle, intensity);
	} 
	
	@Override
	public void newTextureReady(int texture, GLTextureOutputRenderer source) {
		if(filterLocations.size() < 2 || !filterLocations.contains(source)) {
			clearRegisteredFilterLocations();
			registerFilterLocation(source, 0);
			registerFilterLocation(blur, 1);
			registerInputOutputFilter(source);
		}
		super.newTextureReady(texture, source);
	}
	
	@Override
	protected String getFragmentShader() {
		return
				 "precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n" 
				+"uniform sampler2D "+UNIFORM_TEXTUREBASE+1+";\n"
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform float "+UNIFORM_INTENSITY+";\n"
						
				
		  		+"void main(){\n"
				+"   vec4 sharpImageColor = texture2D("+UNIFORM_TEXTURE0+", "+VARYING_TEXCOORD+");\n"
				+"   vec4 blurredImageColor = texture2D("+UNIFORM_TEXTUREBASE+1+", "+VARYING_TEXCOORD+");\n"
		  		+"   gl_FragColor = vec4(mix(sharpImageColor.rgb, blurredImageColor.rgb, "+UNIFORM_INTENSITY+"), sharpImageColor.a);\n"
		  		+"}\n";
	}
}
