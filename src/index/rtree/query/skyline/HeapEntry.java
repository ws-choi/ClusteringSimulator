package index.rtree.query.skyline;

import index.rtree.dimitris.Comparable;

public class HeapEntry implements Comparable{

	
	public float dist;
	public float[] mbr;

	public void setDist(float dist) {
		this.dist = dist;
	}
	
	public float getDist() {
		return dist;
	}
	
	@Override
	public int compare(Object other) {
		
		if ( !(other instanceof HeapEntry) ) return -1;
		
		HeapEntry you = (HeapEntry) other;
		
		float mydist = dist;
		float yourdist = you.dist;
		
		if (mydist > yourdist) return -1;
		else if (mydist < yourdist) return +1;
		else return 0;
	}
	
}
