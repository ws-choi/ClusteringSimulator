package com.clustering.dbscan.gui;

import gui.communication.CWS_communication;
import gui.communication.CWS_consumer;
import gui.communication.CWS_producer;
import gui.communication.CWS_socket;
import gui.communication.prod_socket;

public class Constants {

	
	public static final int data_to_visual = 1;
	public static final int visual_to_data = 2;
	public static final int data_to_window = 3;
	public static final int window_to_data = 4;
	public static final int cluster_to_window = 5;
	public static final int window_to_cluster = 6;
	
	
	
	public static void connect(CWS_communication prod, CWS_communication cons,	int port) {

		CWS_socket socket = new CWS_socket(prod, cons, port);
		
		prod.set_socket(port, socket);
		cons.set_socket(port, socket);
		
		socket.start();
	}
	
	public static void connect(CWS_producer prod, CWS_consumer cons, int port) {

		prod_socket socket = new prod_socket(cons, port);
		
		prod.set_socket(port, socket);
		
		socket.start();
	}
}
