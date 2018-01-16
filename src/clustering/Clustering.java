package clustering;

import index.rtree.dimitris.Data;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.clustering.dbscan.gui.DataTuple;

abstract public class Clustering implements Runnable {

	protected int dim;
	protected List<DataTuple> datalist;
	protected long sleep_time;
	public static final int unClassified = -1;
	public static final int noise = -2;

	public static final Semaphore sem = new Semaphore(1);;
	
	public Clustering(List<DataTuple> inputData) {
		
		datalist = inputData;
		dim = inputData.get(0).dimension;
		sleep_time = 0;
		
		try {
			initialize();
		} catch (Exception e) {
			
			e.printStackTrace();
		}	

	}
	
	protected void initialize () throws Exception{
		
		if(!sem.tryAcquire()) throw new Exception();
		
		LinkedList<DataTuple> removeList = new LinkedList<DataTuple>();
		Iterator<DataTuple> iter = datalist.iterator();
		
		while(iter.hasNext()){
			DataTuple tuple = iter.next();
			if(tuple.isSpecial()) removeList.add(tuple);
		}
		
		while(!removeList.isEmpty())
			datalist.remove(removeList.remove());
		
		for (DataTuple tuple : datalist) {
			tuple.cluster_id=unClassified;
		}
		
		sem.release();
	}
	
	abstract public void cluster() throws Exception;
	
	@Override
	public void run() {
		
		try {
			
			if(!sem.tryAcquire()) 
				throw new ConcurrentModificationException("only one clustering allowed!");
			
			cluster();
			sem.release();				
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void set_sleep (long time)
	{
		this.sleep_time = time;
	}
	
	protected void chg_clu_id (DataTuple data, int cluster_id)
	{
		if(data.isSpecial()) return;
		
		data.cluster_id=cluster_id;

		take_sleep();
	}
	protected void take_sleep() {
		take_sleep(sleep_time);
	}

	protected void take_sleep(long sleep) {
		
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected float get_dist (Data point1, Data point2){

        float sum = (float)0;
        int i;

        for( i = 0; i < point1.dimension; i++)
            sum += java.lang.Math.pow(point1.data[2*i] - point2.data[2*i], 2);

        return(sum);
	}
	
	protected int get_min_index (float[] input){
		
		float min = Float.MAX_VALUE;
		int index = -1;
		
		for (int i = 0; i < input.length; i++) {
			float f = input[i];
			if(min > f){
				min = f;
				index = i;
			}
		}
		
		return index;
	}
	
}
