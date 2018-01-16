package clustering;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.clustering.dbscan.gui.DataTuple;

import data.generator.UniformDG;

public class KMeans extends Clustering{

	int k;
	ArrayList<DataTuple> seeds;
	private boolean traceable; 
	
	public KMeans(LinkedList<DataTuple> inputData, int k) {
		super(inputData);
		this.k=k;
		traceable =false;
	}
	
	public KMeans(LinkedList<DataTuple> inputData, int k, long sleep) {
		this(inputData, k);
		this.sleep_time=sleep;
	}
	
	public KMeans(LinkedList<DataTuple> inputData, int k, long sleep, boolean traceable) {
		this(inputData, k, sleep);
		this.traceable = traceable;
	}
	
	@Override
	public void cluster() throws Exception {
		
		boolean terminate = false;

		gen_seeds();
		
		while(!terminate){
			
			if(traceable)
				init_clu_id(datalist);
			
			ArrayList<DataTuple> backup = copy_of(seeds);
			
			re_clustering(seeds, datalist);
			
			terminate = terminate_condition(backup, seeds, 0);
		}
	}

	private void init_clu_id(List<DataTuple> datalist) {
		
		for (DataTuple dataTuple : datalist) 
			if(!dataTuple.isSpecial())
				dataTuple.cluster_id = unClassified;
		
		take_sleep();
		
	}
	
	private ArrayList<DataTuple> copy_of(ArrayList<DataTuple> seeds2) {
		
		ArrayList<DataTuple> copy = new ArrayList<DataTuple>();
		
		Iterator<DataTuple> iter = seeds2.iterator();
		
		while(iter.hasNext())
			copy.add((DataTuple)iter.next().clone());
		
		return copy;
	}

	private void re_clustering(java.util.List<DataTuple> seeds, List<DataTuple> datalist) {
		
		re_assign_data(datalist);
		
		assign_means_to(seeds, datalist);
				
	}

	private void assign_means_to(List<DataTuple> seeds2, List<DataTuple> datalist) {
		
		int[] cluster_size = new int[k];
		
		init_seeds();
		
		for (DataTuple dataTuple : datalist) {
			
			if(dataTuple.isSpecial()) continue;
			
			int c_id = dataTuple.cluster_id;
			
			DataTuple seed = seeds.get(c_id);
			
			for (int i = 0; i < seed.dimension; i++) {
				seed.data[2*i] += dataTuple.data[2*i];
				seed.data[2*i+1] += dataTuple.data[2*i+1];
			} 
			
			cluster_size[c_id] ++;	
		}
		
		for (int j = 0; j < seeds.size(); j++) {
			DataTuple seed = seeds.get(j);
			for (int i = 0; i < seed.data.length; i++) {
				seed.data[i] /= cluster_size[j];
			}
			
			if(traceable)
				take_sleep();
		}
		
	}

	private void re_assign_data(List<DataTuple> list) {
	
		float[] dists = new float[k];
	
		for (DataTuple dataTuple : list) {

			for (int i = 0; i < seeds.size(); i++)
				dists[i] = get_dist(dataTuple, seeds.get(i));
			
			chg_clu_id(dataTuple, get_min_index(dists));
		}
	}

	private boolean terminate_condition(List<DataTuple> seed, ArrayList<DataTuple> seed_copy, float delta) {
		
		for (int j = 0; j < k; j++)
			if(get_dist(seed.get(j), seed_copy.get(j)) > delta)
				return false;
		
		
		return true;
	}

	private void gen_seeds() {

		seeds = new ArrayList<DataTuple>(k);
		UniformDG udg = get_seed_gen();
		
		
		for (int i = 0; i < k; i++) 
		{
			DataTuple tuple = new DataTuple(udg.getNext(), dim);
			tuple.special = true;
			tuple.cluster_id=i;
			seeds.add(tuple) ;
			datalist.add(tuple);
		}
				
	}
	
	private void init_seeds() {
		
		for (DataTuple seed : seeds) 
		{
		
			float[] data = seed.data;
			
			for (int i = 0; i < data.length; i++)
				data[i] = 0;
		
			seed.distanz = 0;
		}
		
		
			
		
	}
	
	private UniformDG get_seed_gen (){
		
		UniformDG udg=null;
		try {
			DataTuple data_range = get_Range(datalist);
			udg = new UniformDG(dim, data_range, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return udg;
	}
	
	private DataTuple get_Range (List<DataTuple> list){

		DataTuple dataRange = new DataTuple(dim);
		
		for (int i = 0; i < dim; i++) {
			
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			
			Iterator<DataTuple> iter = list.iterator();
			
			while(iter.hasNext())
			{
				DataTuple obj = iter.next();
				
				if(min > obj.data[2*i])
					min = obj.data[2*i];
				
				if(max < obj.data[2*1+1])
					max = obj.data[2*1+1];
			}
			
			dataRange.data[2*i] = min;
			dataRange.data[2*i+1] = max;
			
		}
		
		return dataRange;
	}


}
