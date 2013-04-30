package project.android.imageprocessing.filter.colour;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import project.android.imageprocessing.filter.MultiInputFilter;
import project.android.imageprocessing.input.GLTextureOutputRenderer;
import android.graphics.Point;
import android.opengl.GLES20;

public class ToneCurveFilter extends MultiInputFilter {
	private int[] redPart;
	private int[] greenPart;
	private int[] bluePart;
	private int[] splineTexture;
	
	
	public ToneCurveFilter(Point[] red, Point[] green, Point[] blue, Point[] rgbComposite) {
		super(2);
		float[] redCurve = getPreparedSpline(red);
		float[] blueCurve = getPreparedSpline(blue);
		float[] greenCurve = getPreparedSpline(green);
		float[] rgbCompositeCurve = getPreparedSpline(rgbComposite);
		
		redPart = new int[256];
		greenPart = new int[256];
		bluePart = new int[256];
		
		for (int i = 0; i < 256; i++) {
            redPart[i] = (int) Math.min(Math.max(i + redCurve[i] + rgbCompositeCurve[i], 0), 255);
            greenPart[i] = (int) Math.min(Math.max(i + greenCurve[i] + rgbCompositeCurve[i], 0), 255);
            bluePart[i] = (int) Math.min(Math.max(i + blueCurve[i] + rgbCompositeCurve[i], 0), 255);
        }
	}
	
	private float[] getPreparedSpline(Point[] points) {
		Arrays.sort(points, new Comparator<Point>() {
			@Override
			public int compare(Point lhs, Point rhs) {
				return lhs.x - rhs.x;
			}
		});
		
		List<Point> spline = getSplineCurve(points);
        // If we have a first point like (0.3, 0) we'll be missing some points at the beginning
        // that should be 0.		
		if(spline.get(0).x > 0) {
			for(int i = spline.get(0).x; i >= 0; i--) {
				spline.add(0, new Point(i,0));
			}
		}

        // Insert points similarly at the end, if necessary.
		if(spline.get(spline.size()-1).x < 255) {
			for(int i = spline.get(spline.size()-1).x; i < 256; i++) {
				spline.add(new Point(i,255));
			}
		}
		
		// Prepare the spline points.
        float[] preparedSplinePoints = new float[spline.size()];
        for (int i=0; i<spline.size(); i++) 
        {
            Point newPoint = spline.get(i);
            Point origPoint = new Point(newPoint.x, newPoint.x);
            
            float distance = (float) Math.sqrt(Math.pow((origPoint.x - newPoint.x), 2.0) + Math.pow((origPoint.y - newPoint.y), 2.0));
            
            if (origPoint.y > newPoint.y) 
            {
                distance = -distance;
            }
            
            preparedSplinePoints[i] = distance;
        }
        
        return preparedSplinePoints;
	}
	
	private List<Point> getSplineCurve(Point[] points) {
	    double[] sdA = secondDerivative(points);
	    
	    int n = sdA.length;
	    if (n < 1) {
	        return null;
	    }
	    
	    
	    List<Point> output = new ArrayList<Point>(n+1);
	                              
	    for(int i=0; i<n-1 ; i++) {
	        Point cur = points[i];
	        Point next = points[(i+1)];
	        
	        for(int x=cur.x;x<(int)next.x;x++) {
	            double t = (double)(x-cur.x)/(next.x-cur.x);
	            
	            double a = 1-t;
	            double b = t;
	            double h = next.x-cur.x;
	            
	            double y= a*cur.y + b*next.y + (h*h/6)*( (a*a*a-a)*sdA[i]+ (b*b*b-b)*sdA[i+1] );
	                        
	            if (y > 255.0) {
	                y = 255.0;   
	            } else if (y < 0.0) {
	                y = 0.0;   
	            }
	            
	           output.add(new Point(x,(int)y));
	        }
	    }
	    
	    if(output.size() == 255) {
	    	output.add(points[points.length-1]);
	    }
	    
	    return output;
	}

	private double[] secondDerivative(Point[] points) {
	    int n = points.length;
	    if (n <= 1) {
	        return null;
	    }
	    
	    double[][] matrix = new double[n][3];
	    double[] result = new double[n];
	    matrix[0][1]=1;
	    // What about matrix[0][1] and matrix[0][0]? Assuming 0 for now (Brad L.)
	    matrix[0][0]=0;    
	    matrix[0][2]=0;    
	    
	    for(int i=1;i<n-1;i++) {
	        Point P1 = points[(i-1)];
	        Point P2 = points[i];
	        Point P3 = points[(i+1)];
	        
	        matrix[i][0]=(double)(P2.x-P1.x)/6;
	        matrix[i][1]=(double)(P3.x-P1.x)/3;
	        matrix[i][2]=(double)(P3.x-P2.x)/6;
	        result[i]=(double)(P3.y-P2.y)/(P3.x-P2.x) - (double)(P2.y-P1.y)/(P2.x-P1.x);
	    }
	    
	    // What about result[0] and result[n-1]? Assuming 0 for now (Brad L.)
	    result[0] = 0;
	    result[n-1] = 0;

	    matrix[n-1][1]=1;
	    // What about matrix[n-1][0] and matrix[n-1][2]? For now, assuming they are 0 (Brad L.)
	    matrix[n-1][0]=0;
	    matrix[n-1][2]=0;
	    
	  	// solving pass1 (up->down)
	  	for(int i=1;i<n;i++) 
	    {
			double k = matrix[i][0]/matrix[i-1][1];
			matrix[i][1] -= k*matrix[i-1][2];
			matrix[i][0] = 0;
			result[i] -= k*result[i-1];
	    }
		// solving pass2 (down->up)
		for(int i=n-2;i>=0;i--) 
	    {
			double k = matrix[i][2]/matrix[i+1][1];
			matrix[i][1] -= k*matrix[i+1][0];
			matrix[i][2] = 0;
			result[i] -= k*result[i+1];
		}
	    
	    double[] y2 = new double[n];
	    for(int i=0;i<n;i++) y2[i]=result[i]/matrix[i][1];
	    
	    return y2;
	}
	
	@Override
	public void newTextureReady(int texture, GLTextureOutputRenderer source) {
		if(filterLocations.size() < 2 || !source.equals(filterLocations.get(0))) {
			clearRegisteredFilterLocations();
			registerFilterLocation(source, 0);
			registerFilterLocation(this, 1);
		}
		if(splineTexture == null || splineTexture[0] == 0) {
			createSplineTexture();
		}
		super.newTextureReady(splineTexture[0], this);
		super.newTextureReady(texture, source);
	}
	
	
	@Override
	public void destroy() {
		super.destroy();
		if(splineTexture != null && splineTexture[0] != 0) {
			GLES20.glDeleteTextures(1, splineTexture, 0);
			splineTexture = null;
		}
	}
	
	private void createSplineTexture() {		
		int[] data = new int[256];
		for(int i = 0; i < 256; i++) {
			data[i] = (redPart[i] & 0x000000FF) | ((greenPart[i] << 8) & 0x0000FF00) | ((bluePart[i] << 16) & 0x00FF0000) | 0xFF000000;
		}
		
		splineTexture = new int[1];
		GLES20.glGenTextures(1, splineTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, splineTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256, 1, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, IntBuffer.wrap(data));
	}

	@Override
	protected String getFragmentShader() {
		return 
				 "precision mediump float;\n" 
				+"uniform sampler2D "+UNIFORM_TEXTURE0+";\n" 
				+"uniform sampler2D "+UNIFORM_TEXTUREBASE+1+";\n" 
				+"varying vec2 "+VARYING_TEXCOORD+";\n"	
				+"const float halfPixelWidth = 1.0/512.0;"
				
		  		+ "void main(){\n"
		  		+ "   vec4 texColour = texture2D("+UNIFORM_TEXTURE0+","+VARYING_TEXCOORD+");\n"
		  		+ "   float rVal;\n"
		  		+ "   if(texColour.r < halfPixelWidth) {"
		  		+ "     rVal = texture2D("+UNIFORM_TEXTUREBASE+1+", vec2(texColour.r + halfPixelWidth, 0.5)).r;\n"
		  		+ "   } else {\n"
		  		+ "     rVal = texture2D("+UNIFORM_TEXTUREBASE+1+", vec2(texColour.r - halfPixelWidth, 0.5)).r;\n"
		  		+ "   }\n"
		  		+ "   float gVal;\n"
		  		+ "   if(texColour.g < halfPixelWidth) {"
		  		+ "     gVal = texture2D("+UNIFORM_TEXTUREBASE+1+", vec2(texColour.g + halfPixelWidth, 0.5)).r;\n"
		  		+ "   } else {\n"
		  		+ "     gVal = texture2D("+UNIFORM_TEXTUREBASE+1+", vec2(texColour.g - halfPixelWidth, 0.5)).r;\n"
		  		+ "   }\n"
		  		+ "   float bVal;\n"
		  		+ "   if(texColour.b < halfPixelWidth) {"
		  		+ "     bVal = texture2D("+UNIFORM_TEXTUREBASE+1+", vec2(texColour.b + halfPixelWidth, 0.5)).r;\n"
		  		+ "   } else {\n"
		  		+ "     bVal = texture2D("+UNIFORM_TEXTUREBASE+1+", vec2(texColour.b - halfPixelWidth, 0.5)).r;\n"
		  		+ "   }\n"
		  		+ "   gl_FragColor = vec4(rVal,gVal,bVal,texColour.a);\n"
		  		+ "}\n";		
	}
}
