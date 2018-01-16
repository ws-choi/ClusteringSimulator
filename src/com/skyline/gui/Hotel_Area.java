package com.skyline.gui;

import gui.communication.CWS_consumer;
import gui.communication.CWS_producer;
import gui.communication.prod_socket;
import gui.dialog.Insert_dialog;
import gui.event.My_Event;
import index.rtree.dimitris.DirEntry;
import index.rtree.dimitris.RTDataNode;
import index.rtree.dimitris.RTDirNode;
import index.rtree.dimitris.RTNode;
import index.rtree.dimitris.RTree;
import index.rtree.query.event.Skyline_Query_Event;
import index.rtree.query.skyline.hotel.Hotel;
import index.rtree.query.skyline.hotel.Hotel_Sky_QE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.skyline.global.MyConstants;

import data.Point_Float;

enum type {insertion, normal_sky, hotel_sky};


public class Hotel_Area extends JPanel implements MouseMotionListener, MouseListener, CWS_consumer, CWS_producer{

	private static final long serialVersionUID = -4083085046104562883L;
	public JFrame gui;
	RTree tree;

	public prod_socket this_to_Single;

	private Point cursor_point;
	private type click_type;
	
	public Hotel_Area(JFrame gui) {
		
		click_type = type.insertion; 
		this.gui = gui;
		addMouseMotionListener(this);
		addMouseListener(this);
		
	}

	private int get_circle_level (float price, int num, float min, float max){
		
		return (int)((int)(price-min)/(max-min)*num) + 1;
		
	}
	
	@Override
	public void paint(Graphics g) {
		
		super.paint(g);
		
		if(tree!=null)
		{
			RTree rt = tree;
			RTNode node = rt.root_ptr;			
			print_Data(node, g);

		}
	}
	
	private void print_Data(RTNode node, Graphics g) {


		if(node instanceof RTDataNode)
		{
			 RTDataNode datanode = (RTDataNode)node;

			 for (int i = 0; i < datanode.get_num_of_data(); i++) {
				
	/*			Data data = datanode.get(i);
				
				
				g.drawString("X"+data.data[4],(int) data.data[0], (int)data.data[2]);
				*/
				Hotel hotel = (Hotel) datanode.get(i);
				
				if(hotel.selected)
					g.setColor(Color.red);
				else g.setColor(Color.black);
				
				int circle_level = get_circle_level(hotel.data[4], 10, 100, 500);
				Graphics2D g2d = (Graphics2D) g;
				
				Ellipse2D.Double circle = new Ellipse2D.Double((int) hotel.data[0]-circle_level*4, (int)hotel.data[2]-circle_level*4, circle_level*8, circle_level*8);

				if(hotel.selected)
					g2d.fill(circle);
				else 
					g2d.draw(circle);
//				g.fillRoundRect((int) hotel.data[0], (int)hotel.data[2], ((int)hotel.data[4]/100),((int)hotel.data[4]/100),1,1);
				g.drawString("X"+hotel.data[4],(int) hotel.data[0], (int)hotel.data[2]);
				
				
			}
		}
		
		else {
			 
			RTDirNode dirnode = (RTDirNode)node;
			DirEntry[] entries = dirnode.get_entries();

			for (int i = 0; i < dirnode.get_num(); i++) {
				print_Data(entries[i].get_son(), g);
			}
			
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		cursor_point = arg0.getPoint();
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		
		if(click_type == type.insertion)
			new Insert_dialog(this, cursor_point);

		else if(click_type == type.normal_sky)
			produce_somthing(MyConstants.hts, new Skyline_Query_Event(new Point_Float(cursor_point.x, cursor_point.y)));
			
		else if(click_type == type.hotel_sky)
			produce_somthing(MyConstants.hts, new Hotel_Sky_QE(new Point_Float(cursor_point.x, cursor_point.y)));
			
		
	}


	public void produce_somthing(int port, My_Event my_Evnet) {
		
		System.out.println(this_to_Single);
		if(port == MyConstants.hts)
			this_to_Single.notify_event(my_Evnet);
	}



	@Override
	public void consume_something(int port, My_Event my_Event) {

		switch (port) {
		case MyConstants.ith:
			
			Integer input_case = (Integer) my_Event.carrier;
			System.out.println(input_case);
			if(input_case == MyConstants.insert) click_type = type.insertion; 
			else if (input_case == MyConstants.normal_skyline) click_type = type.normal_sky;
			else if (input_case == MyConstants.hotel_skyline) click_type = type.hotel_sky;
			
			break;

		case MyConstants.rth:
			
			tree = (RTree) my_Event.carrier;
			repaint();
			System.gc();
		default:
			break;
		}
	}
	
	
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseDragged(MouseEvent arg0) {}



	@Override
	public void set_socket(int port, prod_socket cws_socket) {

		switch (port) {
		
		case MyConstants.hts:
			this_to_Single = cws_socket;

		default:
			break;
		}		
	}


	
}

