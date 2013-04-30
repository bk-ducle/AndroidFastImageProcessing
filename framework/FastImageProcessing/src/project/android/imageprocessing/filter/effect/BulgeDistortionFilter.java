package project.android.imageprocessing.filter.effect;

import project.android.imageprocessing.filter.BasicFilter;
import android.graphics.PointF;
import android.opengl.GLES20;

public class BulgeDistortionFilter extends BasicFilter {
	protected static final String UNIFORM_CENTER = "u_Center";
	protected static final String UNIFORM_RADIUS = "u_Radius";
	protected static final String UNIFORM_DISTORTION_AMOUNT = "u_DistortionAmount";
	protected static final String UNIFORM_ASPECT_RATIO = "u_AspectRatio";
	
	private int centerHandle;
	private int radiusHandle;
	private int distortionAmountHandle;
	private int aspectRatioHandle;
	private float radius;
	private PointF center;
	private float distortionAmount;
	private float aspectRatio;
	
	public BulgeDistortionFilter(PointF center, float radius, float distortionAmount, float aspectRatio) {
		this.center = center;
		this.radius = radius;
		this.distortionAmount = distortionAmount;
		this.aspectRatio = aspectRatio;
	}
		
	@Override
	protected void initShaderHandles() {
		super.initShaderHandles();
		centerHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_CENTER);
		radiusHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_RADIUS);
		distortionAmountHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_DISTORTION_AMOUNT);
		aspectRatioHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_ASPECT_RATIO);
	}
	
	@Override
	protected void passShaderValues() {
		super.passShaderValues();
		GLES20.glUniform2f(centerHandle, center.x, center.y);
		GLES20.glUniform1f(radiusHandle, radius);
		GLES20.glUniform1f(distortionAmountHandle, distortionAmount);
		GLES20.glUniform1f(aspectRatioHandle, aspectRatio);
	}
	
	@Override
	protected String getFragmentShader() {
		return 
				 "precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"uniform vec2 "+UNIFORM_CENTER+";\n"
				+"uniform float "+UNIFORM_RADIUS+";\n"
				+"uniform float "+UNIFORM_DISTORTION_AMOUNT+";\n"
				+"uniform float "+UNIFORM_ASPECT_RATIO+";\n"
				
		  		+"void main(){\n"
			    +"   highp vec2 textureCoordinateToUse = vec2("+VARYING_TEXCOORD+".x, ("+VARYING_TEXCOORD+".y * "+UNIFORM_ASPECT_RATIO+" + 0.5 - 0.5 * "+UNIFORM_ASPECT_RATIO+"));\n"
			    +"   highp float dist = distance("+UNIFORM_CENTER+", textureCoordinateToUse);\n" 
			    +"   textureCoordinateToUse = "+VARYING_TEXCOORD+";\n"
			    +"   if (dist < "+UNIFORM_RADIUS+") {\n"
			    +"     textureCoordinateToUse -= "+UNIFORM_CENTER+";\n"
			    +"     highp float percent = 1.0 - ("+UNIFORM_RADIUS+" - dist) / "+UNIFORM_RADIUS+" * "+UNIFORM_DISTORTION_AMOUNT+";\n"
			    +"     percent = percent * percent;\n"
			    +"     textureCoordinateToUse = textureCoordinateToUse * percent;\n"
			    +"     textureCoordinateToUse += "+UNIFORM_CENTER+";\n"
			    +"   }\n"
			    +"   gl_FragColor = texture2D("+UNIFORM_TEXTURE0+", textureCoordinateToUse);\n"
		  		+"}\n";
	}
}
