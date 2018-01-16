package com.clustering.dbscan.gui;

import clustering.Clustering;
import index.rtree.dimitris.Data;

public class DataTuple extends Data {

	private static int cnt = 0;
	
	public int cluster_id;
	
	public boolean special;

	public DataTuple(int dim) {
		super(dim);
		special=false;
	}
	public DataTuple( Data input, int dim) {
		
		dimension = dim;
		
		data = new float[dim*2];
		for (int i = 0; i < dim * 2; i++) {
			data[i] = input.data[i];
		}
		
		cluster_id = -1;
		id = cnt++;
		
		special=false;
	}

	
	public Object clone()
	  {
	    DataTuple d = new DataTuple(this.dimension);
	    
	    d.distanz = this.distanz;
	    //for (int i = 0; i < this.dimension; ++i)
	    for (int i = 0; i < this.dimension*2; ++i)
	        d.data[i] = this.data[i];
	    d.id = this.id;
	    d.cluster_id = this.cluster_id;
	    return (Object)d;
	  }

	public boolean isUnClassified () {
		return 	(cluster_id == Clustering.unClassified);
	}

	public boolean isNoise () {
		return 	(cluster_id == Clustering.noise);
	}
	
	public boolean isSpecial () {
		return 	special;
	}
	
	
	
}
