package com.skyline.global;

import gui.communication.CWS_communication;
import gui.communication.CWS_consumer;
import gui.communication.CWS_producer;
import gui.communication.CWS_socket;
import gui.communication.prod_socket;
import index.basic_ds.SortedLinList;
import index.rtree.dimitris.Data;
import index.rtree.query.skyline.HeapEntry;
import data.Point_Float;

public class MyConstants {

	public static final int dimension = 3;
	public static final int hts = 1;
	public static final int ith = 2;
	public static final int dts = 3;
	public static final int std = 4;
	public static final int rts = 5;
	public static final int rth = 6;
	
	public static final int insert = 0;
	public static final int normal_skyline = 1;
	public static final int hotel_skyline = 2;
	
	public static final float[] price_bounds = {100, 500};
	
	public static void connect(CWS_communication prod, CWS_communication cons,	int port) {

		CWS_socket socket = new CWS_socket(prod, cons, port);
		
		prod.set_socket(port, socket);
		cons.set_socket(port, socket);
		
		socket.start();
	}

	
	public static boolean isDominated (float[] mbr, SortedLinList list){
		
		boolean result = false;

		for(int i=0; i<list.get_num(); i++)
		{
			boolean dominated = true;

			HeapEntry skyline_obj = (HeapEntry) list.get(i);
			for(int j =0 ; j<dimension; j++)				
				if(skyline_obj.mbr[2*j+1] > mbr[2*j])
					dominated = false;
			
			if(dominated) return true;

		}
		
		return result;
	}
		
	public static boolean isDominated_hotel (Point_Float point, float[] mbr, SortedLinList list){
		
		boolean result = false;
		

		float[] mbrxy = new float[4];
		for (int i = 0; i < mbrxy.length; i++) {
			mbrxy[i] = mbr[i];
		}
		float my = MINDIST(point.toArray(), mbrxy);

		for(int i=0; i<list.get_num(); i++)
		{
			boolean dominated = true;

			HeapEntry skyline_obj = (HeapEntry) list.get(i);
			
			if(skyline_obj.mbr[5] > mbr[4]) dominated=false;

			
			float[] skymbr = new float[4];
			for (int j = 0; j < skymbr.length; j++) {
				skymbr[j]=skyline_obj.mbr[j];
			}
			
			float yours = MINDIST(point.toArray(), skymbr);
			
			if(yours > my) dominated = false;
			
			if(dominated) return true;

		}
		
		return result;
	}
	
	public static void connect(CWS_producer prod, CWS_consumer cons, int port) {

		prod_socket socket = new prod_socket(cons, port);
		
		prod.set_socket(port, socket);
		
		socket.start();
	}
	
	public static void error(String string, boolean b) {
		// TODO Auto-generated method stub
		
	}
	
    public static float MINDIST(Data point, float bounces[])
    {
        //
        // Berechne die kuerzeste Entfernung zwischen einem Punkt Point
        // und einem MBR bounces (Lotrecht!)
        //

        
        float sum = (float)0.0;
        float r;
        int i;

        for(i = 0; i < point.dimension; i++)
        {
            if (point.data[i] < bounces[2*i])
                r = bounces[2*i];
            else
            {
                if (point.data[i] > bounces[2*i+1])
                            r = bounces[2*i+1];
                else
                            r = point.data[i];
            }

            sum += java.lang.Math.pow(point.data[i] - r , 2);
        }
        return(sum);
    }
	
    public static float MINDIST(float[] point, float bounces[])
    {
        //
        // Berechne die kuerzeste Entfernung zwischen einem Punkt Point
        // und einem MBR bounces (Lotrecht!)
        //

        
        float sum = (float)0.0;
        float r;
        int i;

        int dimension = point.length;
        for(i = 0; i < dimension; i++)
        {
            if (point[i] < bounces[2*i])
                r = bounces[2*i];
            else
            {
                if (point[i] > bounces[2*i+1])
                            r = bounces[2*i+1];
                else
                            r = point[i];
            }

            sum += java.lang.Math.pow(point[i] - r , 2);
        }
        return(sum);
    }
}
