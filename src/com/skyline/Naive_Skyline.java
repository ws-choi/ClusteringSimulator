package com.skyline;

import index.rtree.dimitris.Data;

import java.util.LinkedList;

public class Naive_Skyline implements  Runnable {

	LinkedList<Data> list;
	
	public Naive_Skyline(LinkedList<Data> list) {
		this.list=list;
	}
	
	@Override
	public void run() {
		
	}

	public LinkedList<Data> skyline() {
		
		LinkedList<Data> result = new LinkedList<Data>();
				
		for (Data me : list) {
			boolean imALooser = false;
			for (Data you : list) {
				if(isDominated(me, you))
				{
					imALooser = true;
					break;
				}
				else continue;
			}
			if(!imALooser) result.add(me);
		}
		
		return result;
	}
	

	public static boolean isDominated (Data me, Data you){
		
		boolean same = true;

		for(int j =0 ; j<me.dimension; j++)				
				if(me.data[2*j+1] < you.data[2*j])
					return false;
				else if(me.data[2*j+1] > you.data[2*j])
					same = false;
					
		if(!same) return true;
		else return false;
			
	}


}
