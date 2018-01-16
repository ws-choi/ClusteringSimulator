package clustering;

import index.basic_ds.SortedLinList;
import index.rtree.dimitris.PPoint;
import index.rtree.dimitris.RTree;

import java.util.LinkedList;

import com.clustering.dbscan.gui.DataTuple;

public class DBSCAN_Rtree extends DBSCAN {

	RTree tree;
	
	public DBSCAN_Rtree(LinkedList<DataTuple> inputData, float eps, int minPts) {
		
		super(inputData, eps, minPts);
				
		load_tree();
	
	}
	
	public DBSCAN_Rtree(LinkedList<DataTuple> inputData, float eps, int minPts, long sleep) {
		super(inputData, eps, minPts, sleep);
		
		load_tree();
	}
	
	

	private void load_tree() {
		tree = null;
		System.gc();
		tree = new RTree("dbscan.r", 256, 0, dim);
		
		for (DataTuple dataTuple : datalist)
			tree.insert(dataTuple);
		
	}



	@Override
	protected LinkedList<DataTuple> regionQuery(DataTuple point, float eps) {
		
		LinkedList<DataTuple> result = new LinkedList<DataTuple>();
		SortedLinList res = new SortedLinList();
		
		PPoint ppoint = new PPoint(dim);
		
		for (int i = 0; i < dim; i++) {
			ppoint.data[i] = point.data[2*i];
		}
		
		tree.rangeQuery(ppoint, eps, res);
		
		while( res.get_num() > 0)
		{
			result.add((DataTuple)res.get_first());
			res.erase();
		}
		
	
		
		return result;
	}

}
