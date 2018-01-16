package index.rtree.query.skyline.hotel;

import gui.event.My_Event;

import java.awt.Point;


public class HtS_Event extends My_Event{
	
	public float price;
	public Point point;
	
	public HtS_Event(float price, Point point) {
		this.point = point;
		this.price = price;
	}

}
