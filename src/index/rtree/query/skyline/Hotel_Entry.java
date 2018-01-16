package index.rtree.query.skyline;

import index.rtree.dimitris.Data;

import com.skyline.global.MyConstants;

import data.Point_Float;

public class Hotel_Entry extends HeapEntry{

	
	public Data data;
	
	public Hotel_Entry( Data data, Point_Float point ) {
		
		if( !(data instanceof Data) )
			MyConstants.error("not data.", true);
		
		this.data = data;
		
		dist = (data.get_mbr()[0] - point.x) * (data.get_mbr()[0] - point.x) 
				+ (data.get_mbr()[2] - point.y) *(data.get_mbr()[2] - point.y) ;
		
		dist = (float) Math.sqrt(dist);
		dist += data.get_mbr()[4];
		
		mbr = data.data.clone();
		
	}
	
	
}
