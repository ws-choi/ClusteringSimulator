package data;

public class Point_Float {

	public float x,y;
	public Point_Float(float x, float y) {
		
		this.x=x; this.y=y;
	}
	
	public float[] toArray () { float[] res =  new float[2]; res[0]=x; res[1] = y; return res;}
}
