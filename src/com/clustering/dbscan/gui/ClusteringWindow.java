package com.clustering.dbscan.gui;

import gui.communication.CWS_communication;
import gui.communication.CWS_consumer;
import gui.communication.CWS_producer;
import gui.communication.CWS_socket;
import gui.communication.prod_socket;
import gui.event.My_Event;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

import javax.swing.JFrame;

import clustering.Clustering;

import data.generator.DataGenerator;

public class ClusteringWindow extends JFrame implements CWS_communication, CWS_consumer, CWS_producer{
	
	private static final long serialVersionUID = 6690714888571065424L;
	CWS_socket window_to_data;
	VisualizedData visual_data_gui;
	DataGen_GUI data_gen_gui;
	Cluster_GUI cluster_gui;
	prod_socket window_to_cluster;
	
	LinkedList<DataTuple> datalist;
	
	public ClusteringWindow() {

		super("클러스터링 시물레이션 - 고려대학교 최우성");
		//tree = new RTree("dbscan.r", 256, 0, 2);
		datalist = new LinkedList<DataTuple>();

		init();
		exit_handling();
		
		Constants.connect(data_gen_gui, this, Constants.data_to_window);
		Constants.connect(this, data_gen_gui, Constants.window_to_data);
		Constants.connect(cluster_gui, this, Constants.cluster_to_window);
		Constants.connect(this, cluster_gui, Constants.window_to_cluster);
		
	}
	
	
	
	private void init() {
		
		
		setSize(900, 800);
		setLocationRelativeTo(null);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		visual_data_gui = new VisualizedData(datalist);
		getContentPane().add(visual_data_gui, BorderLayout.CENTER);
		
		data_gen_gui = new DataGen_GUI(this);
		getContentPane().add(data_gen_gui, BorderLayout.SOUTH);
		
		cluster_gui = new Cluster_GUI(this);
		getContentPane().add(cluster_gui, BorderLayout.NORTH);
		
	}

	private void exit_handling() {
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});	
		
	}


	public static void main(String[] args) {

		ClusteringWindow cw = new ClusteringWindow();
		
		cw.setVisible(true);
	}



	@Override
	public void produce_somthing(int port, My_Event my_Evnet) {

	}



	@Override
	public void consume_something(int port, My_Event my_Event) {

		switch (port) {
		case Constants.data_to_window:
			
			if(my_Event == null){ //Request VisualizedData Panel Size
				
				//Reply the size
				My_Event event = new Size_Event( visual_data_gui.getSize());
				window_to_data.notify_event(event);
				
			}
			
			else
			{
				if(Clustering.sem.tryAcquire()) 
				{
					DataGenerator data_gen = (DataGenerator) my_Event.carrier;
					
					for (int i = 0; i < 50; i++) {
						datalist.add(new DataTuple(data_gen.getNext(), 2));
						request_sleep(10);
					}	
					
					Clustering.sem.release();
			
				}
				
			}
			break;

		case Constants.cluster_to_window:
			
			if(my_Event == null){
				
				My_Event event = new My_Event();
				event.carrier = datalist;
				window_to_cluster.notify_event(event);
					
			}
			

			
		default:
			break;
		}
	}



	private void request_sleep(long sleep) {
		
	
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}



	@Override
	public void set_socket(int port, CWS_socket cws_socket) {

		switch (port) {
		case Constants.window_to_data:
			window_to_data = cws_socket;
			break;

		default:
			break;
		}
	}



	@Override
	public void set_socket(int port, prod_socket cws_socket) {
		
		switch (port) {
		case Constants.window_to_cluster:
			window_to_cluster = cws_socket;
			break;

		default:
			break;
		}
	}
	
}
