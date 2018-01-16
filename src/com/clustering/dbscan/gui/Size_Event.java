package com.clustering.dbscan.gui;

import gui.event.My_Event;

import java.awt.Dimension;

public class Size_Event extends My_Event {

	public Dimension dim;
	
	public Size_Event(Dimension dim ) {
		this.dim = dim;
	}

}
