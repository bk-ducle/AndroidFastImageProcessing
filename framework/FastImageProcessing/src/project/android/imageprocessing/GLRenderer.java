package project.android.imageprocessing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

/**
 * The base renderer class that all inputs, filters and endpoints must extend.  
 * @author Chris Batt
 */
public abstract class GLRenderer {
	protected static final String ATTRIBUTE_POSITION = "a_Position";
	protected static final String ATTRIBUTE_TEXCOORD = "a_TexCoord";
	protected static final String VARYING_TEXCOORD = "v_TexCoord";
	protected static final String UNIFORM_TEXTUREBASE = "u_Texture";
	protected static final String UNIFORM_TEXTURE0 = UNIFORM_TEXTUREBASE+0;
	
	protected int curRotation;
	protected FloatBuffer renderVertices;
	protected FloatBuffer[] textureVertices;
	
	protected int programHandle;
	private int vertexShaderHandle;
	private int fragmentShaderHandle;
	protected int textureHandle;
	protected int positionHandle;
	protected int texCoordHandle;
	
	protected int texture_in;
	
	private int width;
	private int height;
	
	private boolean customSizeSet;
	private boolean initialized;
	private boolean sizeChanged;
	
	private float red;
	private float green;
	private float blue;
	private float alpha;
	
	public GLRenderer() {
		initialized = false;
		
		setRenderVertices(new float[] {	  	        
	        -1f, -1f,   
	        1f, -1f,	   
			-1f, 1f,       
	        1f, 1f
		});
		
		textureVertices = new FloatBuffer[4];
		
		float[] texData0 = new float[] {
	        0.0f, 0.0f,
	        1.0f, 0.0f,
	        0.0f, 1.0f,
	        1.0f, 1.0f,
		};
		textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
		textureVertices[0].put(texData0).position(0);
		
		float[] texData1 = new float[] {
	        0.0f, 1.0f,
	        0.0f, 0.0f,
	        1.0f, 1.0f,
	        1.0f, 0.0f,
		};
		textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
		textureVertices[1].put(texData1).position(0);
			
		float[] texData2 = new float[] {
	        1.0f, 1.0f,
	        0.0f, 1.0f,
	        1.0f, 0.0f,
	        0.0f, 0.0f,
		};
		textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
		textureVertices[2].put(texData2).position(0);
		
		float[] texData3 = new float[] {
	        1.0f, 0.0f,
	        1.0f, 1.0f,
	        0.0f, 0.0f,
	        0.0f, 1.0f,
		};
		textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
		textureVertices[3].put(texData3).position(0);
		
		curRotation = 0;
		texture_in = 0;
		customSizeSet = false;
		initialized = false;
		sizeChanged = false;
	}
	
	protected void setRenderVertices(float[] vertices) {
		renderVertices = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder. nativeOrder()).asFloatBuffer();
		renderVertices.put(vertices).position(0);
	}
	
	/**
	 * Returns the current width the GLRenderer is rendering at.
	 * @return width
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Returns the current height the GLRenderer is rendering at.
	 * @return height
	 */
	public int getHeight() {
		return height;
	}
	
	protected void setWidth(int width) {
		if(!customSizeSet && this.width != width) {
			this.width = width;
			sizeChanged = true;
		}
	}
	
	protected void setHeight(int height) {
		if(!customSizeSet && this.height != height) {
			this.height = height;
			sizeChanged = true;
		}
	}
	
	/**
	 * Rotates the renderer clockwise by 90 degrees a given number of times.
	 * @param numOfTimes
	 * The number of times this renderer should be rotated clockwise by 90 degrees.
	 */
	public void rotateClockwise90Degrees(int numOfTimes) {
		curRotation += numOfTimes;
		curRotation = curRotation % 4;
		if(numOfTimes%2==1) {
			int temp = width;
			width = height;
			height = temp;
		}
	}
	
	/**
	 * Rotates the renderer counter-clockwise by 90 degrees a given number of times.
	 * @param numOfTimes
	 * The number of times this renderer should be rotated counter-clockwise by 90 degrees.
	 */
	public void rotateCounterClockwise90Degrees(int numOfTimes) {
		curRotation += 4 - (numOfTimes % 4);
		curRotation = curRotation % 4;
		if(numOfTimes%2==1) {
			int temp = width;
			width = height;
			height = temp;
		}
	}
	
	/**
	 * Sets the render size of the renderer to the given width and height. 
	 * This also prevents the size of the renderer from changing automatically 
	 * when one of the source(s) of the renderer has a size change.  If the renderer
	 * has been rotated an odd number of times, the width and height will be swapped.
	 * @param width
	 * The width at which the renderer should draw at.
	 * @param height
	 * The height at which the renderer should draw at.
	 */
	public void setRenderSize(int width, int height) {
		customSizeSet = true;
		if(curRotation%2 == 1) {
			this.width = height;
			this.height = width;
		} else {
			this.width = width;
			this.height = height;
		}
		sizeChanged = true;
	}
	
	protected void passShaderValues() {
		renderVertices.position(0);
		GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, renderVertices);  
		GLES20.glEnableVertexAttribArray(positionHandle); 
		textureVertices[curRotation].position(0);
		GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureVertices[curRotation]);  
		GLES20.glEnableVertexAttribArray(texCoordHandle); 
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_in);
	    GLES20.glUniform1i(textureHandle, 0);
	}
	
	protected void bindShaderAttributes() {
		GLES20.glBindAttribLocation(programHandle, 0, ATTRIBUTE_POSITION);
		GLES20.glBindAttribLocation(programHandle, 1, ATTRIBUTE_TEXCOORD);
	}
	
	protected void initShaderHandles() {
        textureHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_TEXTURE0);
        positionHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);
        texCoordHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_TEXCOORD);
	}
	
	/**
	 * Re-initializes the filter on the next drawing pass.
	 */
	public void reInitialize() {
		initialized = false;
	}
	/**
	 * Draws the given texture using OpenGL and the given vertex and fragment shaders.
	 * Calling of this method is handled by the {@link FastImageProcessingPipeline} or other filters
	 * and should not be called manually.
	 */
	public void onDrawFrame() {
		if(!initialized) {
			initWithGLContext();
			initialized = true;
		}
		if(sizeChanged) {
			handleSizeChange();
			sizeChanged = false;
		}
		drawFrame();
	}
	
	public void destroy() {
		initialized = false;
		if(programHandle != 0) {
			GLES20.glDeleteProgram(programHandle);
			programHandle = 0;
		}
		if(vertexShaderHandle != 0) {
			GLES20.glDeleteShader(vertexShaderHandle);
			vertexShaderHandle = 0;
		}
		if(fragmentShaderHandle != 0) {
			GLES20.glDeleteShader(fragmentShaderHandle);
			fragmentShaderHandle = 0;
		}
		
	}
	
	protected void initWithGLContext() {
		final String vertexShader = getVertexShader();
		final String fragmentShader = getFragmentShader();									

		vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		String errorInfo = "none";
		if (vertexShaderHandle != 0) {
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);
			GLES20.glCompileShader(vertexShaderHandle);
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) {		
				errorInfo = GLES20.glGetShaderInfoLog(vertexShaderHandle);		
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0) {
			throw new RuntimeException(this + ": Could not create vertex shader. Reason: "+ errorInfo);
		}

		fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		if (fragmentShaderHandle != 0) {
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
			GLES20.glCompileShader(fragmentShaderHandle);
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
			if (compileStatus[0] == 0) {	
				errorInfo = GLES20.glGetShaderInfoLog(fragmentShaderHandle);
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}
		if (fragmentShaderHandle == 0) {
			throw new RuntimeException(this+ ": Could not create fragment shader. Reason: "+ errorInfo);
		}

		programHandle = GLES20.glCreateProgram();
		if (programHandle != 0) {
			GLES20.glAttachShader(programHandle, vertexShaderHandle);	
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			bindShaderAttributes();
			
			GLES20.glLinkProgram(programHandle);
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] == 0) {				
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		if (programHandle == 0) {
			throw new RuntimeException("Could not create program.");
		}

        initShaderHandles();
	}
	
	protected void handleSizeChange() {
		
	}
	
	protected void drawFrame() {
		if(texture_in == 0) {
			return;
		}
		GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(programHandle);

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glClearColor(getBackgroundRed(), getBackgroundGreen(), getBackgroundBlue(), getBackgroundAlpha());
		
		passShaderValues();
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4); 
	}
	
	protected String getVertexShader() {
		return 
					"attribute vec4 "+ATTRIBUTE_POSITION+";\n"
				  + "attribute vec2 "+ATTRIBUTE_TEXCOORD+";\n"	
				  + "varying vec2 "+VARYING_TEXCOORD+";\n"	
				  
				  + "void main() {\n"	
				  + "  "+VARYING_TEXCOORD+" = "+ATTRIBUTE_TEXCOORD+";\n"
				  + "   gl_Position = "+ATTRIBUTE_POSITION+";\n"		                                            			 
				  + "}\n";
	}

	protected String getFragmentShader() {
		return 
				 "precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n"  
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				
		  		+ "void main(){\n"
		  		+ "   gl_FragColor = texture2D("+UNIFORM_TEXTURE0+","+VARYING_TEXCOORD+");\n"	
		  		+ "}\n";		
	}
	
	public void setBackgroundColour(float red, float green, float blue, float alpha) {
		this.setBackgroundRed(red);
		this.setBackgroundGreen(green);
		this.setBackgroundBlue(blue);
		this.setBackgroundAlpha(alpha);
	}

	public float getBackgroundRed() {
		return red;
	}

	public void setBackgroundRed(float red) {
		this.red = red;
	}

	public float getBackgroundGreen() {
		return green;
	}

	public void setBackgroundGreen(float green) {
		this.green = green;
	}

	public float getBackgroundBlue() {
		return blue;
	}

	public void setBackgroundBlue(float blue) {
		this.blue = blue;
	}

	public float getBackgroundAlpha() {
		return alpha;
	}

	public void setBackgroundAlpha(float alpha) {
		this.alpha = alpha;
	}
}
