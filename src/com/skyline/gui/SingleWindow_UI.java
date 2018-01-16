package com.skyline.gui;

import gui.communication.CWS_consumer;
import gui.communication.CWS_producer;
import gui.communication.prod_socket;
import gui.event.My_Event;
import index.rtree.dimitris.DirEntry;
import index.rtree.dimitris.RTDataNode;
import index.rtree.dimitris.RTDirNode;
import index.rtree.dimitris.RTNode;
import index.rtree.dimitris.RTree;
import index.rtree.query.Skyline;
import index.rtree.query.event.Skyline_Query_Event;
import index.rtree.query.skyline.hotel.Hotel;
import index.rtree.query.skyline.hotel.Hotel_Sky;
import index.rtree.query.skyline.hotel.Hotel_Sky_QE;
import index.rtree.query.skyline.hotel.HtS_Event;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.skyline.global.MyConstants;

import data.Point_Float;
import data.generator.DataGenerator;

public class SingleWindow_UI extends JFrame implements CWS_producer, CWS_consumer
{
	private static final long serialVersionUID = -5976867110146945759L;
	
	private prod_socket this_TO_DG;
	private R_Tree_Status_Area r_tree_status_area;
	private Hotel_Area hotel_area;
	private input_Controller input_controller;
	private RTree tree;
	private DataGenerator_GUI dataGen_gui;
	
	public SingleWindow_UI() {

		init();
		exit_handling();
		start_GUI();

		hotel_area.tree = tree;

		MyConstants.connect(hotel_area,this, MyConstants.hts);
		MyConstants.connect(input_controller, hotel_area, MyConstants.ith);
		MyConstants.connect(dataGen_gui, this, MyConstants.dts);
		MyConstants.connect(this, dataGen_gui, MyConstants.std);
		MyConstants.connect(r_tree_status_area, this, MyConstants.rts);
		MyConstants.connect(r_tree_status_area, hotel_area, MyConstants.rth);
	}
	
	private void start_GUI() {
		
		setBounds(new Rectangle(100, 100, 800, 800));
		this.setVisible(true);
	}

	private void exit_handling() {
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});	
		
	}

	private void init() {
		
		tree = new RTree("sample.r", 256, 0, 3);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {80, 20};
		gridBagLayout.rowHeights = new int[] {0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0};
		getContentPane().setLayout(gridBagLayout);
		
		input_controller = new input_Controller();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.weighty = 1.0;
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		getContentPane().add(input_controller, gbc_panel_1);
		
		r_tree_status_area = new R_Tree_Status_Area(tree);
		r_tree_status_area.setBackground(Color.WHITE);
		GridBagConstraints gbc_Control_Area = new GridBagConstraints();
		gbc_Control_Area.weightx = 1.0;
		gbc_Control_Area.fill = GridBagConstraints.BOTH;
		gbc_Control_Area.insets = new Insets(0, 0, 5, 5);
		gbc_Control_Area.gridx = 1;
		gbc_Control_Area.gridy = 1;
		getContentPane().add(r_tree_status_area, gbc_Control_Area);
		
		hotel_area = new Hotel_Area(this);
		hotel_area.setBackground(Color.WHITE);
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.weighty = 10.0;
		gbc_panel_2.weightx = 9.0;
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		getContentPane().add(hotel_area, gbc_panel_2);
		
		dataGen_gui = new DataGenerator_GUI(this);
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.weighty = 1.0;
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 2;
		getContentPane().add(dataGen_gui, gbc_panel_3);
		
	}
	public static void main(String[] args) {
		
		new SingleWindow_UI ();
	}

	@Override
	public void consume_something(int port, My_Event my_Event) {
		
		switch (port) {
		case MyConstants.hts:

			if ( my_Event instanceof HtS_Event)
			{
				
				HtS_Event event = (HtS_Event) my_Event;
				Hotel hotel = new Hotel (new Point_Float(event.point.x, event.point.y), event.price, 0);
				tree.insert(hotel);
				repaint();

			}
			
			else if ( my_Event instanceof Hotel_Sky_QE)
			{
				
				tree.load_root();
				clean_selection(tree.root_ptr);
				
				(new Thread( new Hotel_Sky
						(tree, hotel_area,(Skyline_Query_Event) my_Event, 1000) ) ).start();
				
			}
			
			else if ( my_Event instanceof Skyline_Query_Event)
			{

				tree.load_root();
				clean_selection(tree.root_ptr);
				
				(new Thread( new Skyline(tree, hotel_area,(Skyline_Query_Event) my_Event, 1000) ) ).start();
				
			}
			break;

		case MyConstants.dts:
			
			if(my_Event == null)
			{
				Dimension xy = hotel_area.getSize();
				float[][] bounds = { {0,0}, {0,0}, {100, 500} };
				
				bounds[0][1] = xy.width;
				bounds[1][1] = xy.height;
				
				My_Event info = new BoundsInfo(3, bounds, false);
				
				this_TO_DG.notify_event(info);
			}
			else{
				
				DataGenerator dg = (DataGenerator)my_Event.carrier;
				
				for (int i = 0; i < 100; i++) {
					tree.insert(new Hotel(dg.getNext()));

					hotel_area.repaint();
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				System.gc();
				
				
			}
			break;
			
		case MyConstants.rts:
			tree = (RTree)my_Event.carrier;
			break;
		default:
			break;
		}
		
	}


	private void clean_selection (RTNode node) {

		if(node instanceof RTDataNode)
		{
			 RTDataNode datanode = (RTDataNode)node;

			 for (int i = 0; i < datanode.get_num_of_data(); i++) {
				
				Hotel hotel = (Hotel) datanode.get(i);
				
				if(hotel.selected)
					hotel.selected=false;
							
			}
		}
		
		else {
			 
			RTDirNode dirnode = (RTDirNode)node;
			DirEntry[] entries = dirnode.get_entries();

			for (int i = 0; i < dirnode.get_num(); i++) {
				clean_selection(entries[i].get_son());
			}
			
		}
	}


	@Override
	public void set_socket(int port, prod_socket cws_socket) {
		
		switch (port) {
		case MyConstants.std:
			this_TO_DG = cws_socket;

		default:
			break;
		}
		
	}
	
}

