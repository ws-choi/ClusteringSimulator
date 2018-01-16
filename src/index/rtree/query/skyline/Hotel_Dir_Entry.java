package index.rtree.query.skyline;

import index.rtree.dimitris.RTDataNode;
import index.rtree.dimitris.RTDirNode;
import index.rtree.dimitris.RTNode;

import com.skyline.global.MyConstants;

import data.Point_Float;

public class Hotel_Dir_Entry extends HeapEntry {

	

	public RTNode node;
	public boolean isDataNode;
	
	public Hotel_Dir_Entry( RTNode node , Point_Float point ) {
		
/*		if( !(node instanceof RTNode) )
			MyConstants.error("not node.", true);
	*/	
		this.node = node;
		
		if( node instanceof RTDataNode )
		{
			isDataNode = true;
			RTDataNode thisnode = (RTDataNode) node;
			
			float[] cut = new float[4];
			for (int i = 0; i < cut.length; i++) {
				cut[i] = thisnode.get_mbr()[i];
			}
			dist = MyConstants.MINDIST(point.toArray(), cut );
			dist += thisnode.get_mbr()[4];
			
			mbr = thisnode.get_mbr().clone();
		}
		
		else if ( node instanceof RTDirNode)
		{
			isDataNode = false;
			RTDirNode thisnode = (RTDirNode) node;
			
			float[] cut = new float[4];
			for (int i = 0; i < cut.length; i++) {
				cut[i] = thisnode.get_mbr()[i];
			}
			dist = MyConstants.MINDIST(point.toArray(), cut );
			dist += thisnode.get_mbr()[4];

			mbr = thisnode.get_mbr().clone();
		}
		
		else
			MyConstants.error("not node.", true);
	}
	
}
