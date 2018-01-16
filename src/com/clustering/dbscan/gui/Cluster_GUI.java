package com.clustering.dbscan.gui;

import gui.communication.CWS_consumer;
import gui.communication.CWS_producer;
import gui.communication.prod_socket;
import gui.event.My_Event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Cluster_GUI extends JPanel implements CWS_producer, CWS_consumer{
	
	private static final long serialVersionUID = -6624214169385673017L;

	ClusteringWindow frame;
	
	prod_socket cluster_to_window;
	
	int index;
	public Cluster_GUI( ClusteringWindow frame ) {
	
		this.frame = frame;
		
		JLabel lblSelectAlgo = new JLabel("Select Clustering Algorithm");
		add(lblSelectAlgo);
		
		final JComboBox<String> alogirhm = new JComboBox<String>();
		alogirhm.setModel(new DefaultComboBoxModel<String>(new String[] {"k-means Algorithm", "DBSCAN"}));
		add(alogirhm);
		
		JButton btnMiningStart = new JButton("Start Clustering!");
		btnMiningStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				index = alogirhm.getSelectedIndex();
				cluster_to_window.notify_event(null); //request data list.
			}
		});
		add(btnMiningStart);
	}
	@Override
	public void set_socket(int port, prod_socket cws_socket) {
		switch (port) {
		case Constants.cluster_to_window:
			cluster_to_window = cws_socket;
			break;

		default:
			break;
		}
	}
	@Override
	public void consume_something(int port, My_Event my_Event) {

		switch (port) {
		
		case Constants.window_to_cluster: //window answers. now we have a data list!
		
			@SuppressWarnings("unchecked")
			LinkedList<DataTuple> list = (LinkedList<DataTuple>)my_Event.carrier;
			
			
			switch (index) {
			
			case 0:
				
				new Kmeans_Dialog(frame, "Kmeans parameter", "Please select a value for the parameter", list);
				break;
			case 1:
				
				new DBSCAN_Dialog(frame, "DBSCAN parameter", "please select a value for the parameter", list);				
				break;
				

			default:
				break;
			}
			My_Event event = new My_Event();
			event.carrier = new Integer(1);
			cluster_to_window.notify_event(event);
			break;

		default:
			break;
		}
	}
	

}