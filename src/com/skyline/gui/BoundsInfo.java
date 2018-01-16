package com.skyline.gui;

import gui.event.My_Event;


public class BoundsInfo extends My_Event{
	
	public int dim;
	public float[][] bounds;
	public boolean floatEnable;
	
	public BoundsInfo( int dim, float [][] bounds, boolean floatEnable ) {
		this.dim = dim;
		this.bounds = bounds;
		this.floatEnable = floatEnable;
	}

}
