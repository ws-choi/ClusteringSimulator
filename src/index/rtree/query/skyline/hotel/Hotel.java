package index.rtree.query.skyline.hotel;

import index.rtree.dimitris.Data;
import data.Point_Float;

public class Hotel extends Data{

	public boolean selected = false;
	
	public Hotel(int dim) {
		super(dim);
	}
	public Hotel(Point_Float point, float price, int id ) {
		
		super(3, id);

		data[0] = data[1] = point.x;
		data[2] = data[3] = point.y;
		data[4] = data[5] = price;
	}
	
  public Hotel(Data input) {
		
	  super(3, 0);
		
	  for (int i = 0; i < data.length; i++) {
		data[i] = input.data[i];
	}
	  
		  
	}
	public Object clone()
	  {
	    Hotel d = new Hotel(this.dimension);
	    
	    d.distanz = this.distanz;
	    //for (int i = 0; i < this.dimension; ++i)
	    for (int i = 0; i < this.dimension*2; ++i)
	        d.data[i] = this.data[i];
	    d.id = this.id;
	    d.selected = this.selected;
	    return (Object)d;
	  }
	  
}
