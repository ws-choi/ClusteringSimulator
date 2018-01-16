package clustering;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import com.clustering.dbscan.gui.DataTuple;

public abstract class DBSCAN extends Clustering{

	protected float eps;
	protected int minPts;
	
	public DBSCAN(LinkedList<DataTuple> inputData, float eps, int minPts) {

		super(inputData);
		this.setEps(eps);
		this.setMinPts(minPts);	
	}
	
	public DBSCAN(LinkedList<DataTuple> inputData, float eps, int minPts, long sleep) {

		super(inputData);
		this.setEps(eps);
		this.setMinPts(minPts);	
		this.sleep_time = sleep;
	}

	@Override
	public void cluster() throws Exception {
	
		if(!validate_parameter()) throw new InvalidParameterException();
		
		dbscan(eps, minPts);
		
	}
	
	public void dbscan(float eps, int minPts) throws Exception {
		
		int cluster_Id = 1;
		
		for (DataTuple point : datalist) //Point := SetOfPoints.get(i);
			if(point.isUnClassified()) //IF Point.CiId = UNCLASSIFIED THEN
				if(expandCluster(datalist, point, cluster_Id, eps, minPts)) //IF ExpandCluster(SetOfPoints, Point, ClusterId, Eps, MinPts) THEN
					cluster_Id++; //ClusterId := nextId(ClusterId)
					
		
		for (DataTuple point : datalist)
			System.out.println(point.cluster_id);
	}

	private boolean expandCluster(List<DataTuple> datalist,
			DataTuple point, int clu_Id, float eps, int minPts){
		
		LinkedList<DataTuple> seeds = regionQuery(point, eps); //seeds : =SetOfPoints. regionQuery (Point, Eps )
		
		if(seeds.size() < minPts) //IF seeds.size<MinPts THEN // no core point
		{

			chg_clu_id(point, noise); //SetOfPoint. changeCl Id (Point, NOISE)
			return false;
		}
		
		else
		{
			// all points in seeds are density-
			// reachable from Point
			
			for (DataTuple tuple : seeds) {
				chg_clu_id(tuple, clu_Id);
			} // SetOfpoints. changeCiIds ( seeds, C1 Id)

			seeds.remove(point); //seeds .delete (Point)
			
			while(!seeds.isEmpty())	//WHILE seeds <> Empty DO
			{
				
				DataTuple currentP = seeds.get(0); //currentP := seeds.first()
				
				LinkedList<DataTuple> result = regionQuery(currentP, eps); //result := setofPoints.regionQuery(currentP,Eps)
				
				if(result.size() >= minPts)
					for (DataTuple resultP : result) {
						if(resultP.cluster_id == unClassified)
						{
							seeds.add(resultP);
							chg_clu_id(resultP, clu_Id);
						}
						else if (resultP.cluster_id == noise)
							chg_clu_id(resultP, clu_Id);
					}
				
				seeds.remove(0);
			}
			
			return true;	
		}
		
	}

	@Override
	protected void chg_clu_id (DataTuple data, int cluster_id)
	{
		data.cluster_id=cluster_id;
		
		for (DataTuple dataTuple : datalist) {
			
			if(dataTuple.id == data.id)
				dataTuple.cluster_id = cluster_id;
			
		}
		
		take_sleep();
	}

	abstract protected LinkedList<DataTuple> regionQuery(DataTuple point, float eps);
	
	private boolean validate_parameter() {
		
		if(eps<0) return false;
		if(minPts<1) return false;
		return true;

	}

	
	
	
	
	
	
	
	
	public float getEps() {
		return eps;
	}

	public void setEps(float eps) {
		this.eps = eps;
	}

	public int getMinPts() {
		return minPts;
	}

	public void setMinPts(int minPts) {
		this.minPts = minPts;
	}
	
	

}
