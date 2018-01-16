package com.clustering.dbscan.gui;

import gui.dialog.Lbs_Text_Pair;
import gui.dialog.My_Dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import clustering.DBSCAN_Rtree;

public class DBSCAN_Dialog extends My_Dialog{

	JPanel panel;
	Lbs_Text_Pair eps_pan, minPtr_pan, sleep_pan;
	LinkedList<DataTuple> list;
	//				(new Thread(new DBSCAN_Rtree(list, 100, 5, 10))).start();

	
	public DBSCAN_Dialog(JFrame gui, String top_msg, String first_msg, LinkedList<DataTuple> list) {
		
		super(gui, top_msg, first_msg);
		this.list = list;
		init();
	}

	private void init() {
		
		panel = new JPanel();
	
		eps_pan = new Lbs_Text_Pair("Epsilon", 5);
		minPtr_pan = new Lbs_Text_Pair("MinPtr", 5);
		sleep_pan = new Lbs_Text_Pair("Sleep ms", 5);
		
		eps_pan.get_text_field().setText("100");
		minPtr_pan.get_text_field().setText("5");
		sleep_pan.get_text_field().setText("10");
		
		panel.add(eps_pan);
		panel.add(minPtr_pan);
		panel.add(sleep_pan);
		
		add(panel);
		
		pack();
		
		btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				float eps = eps_pan.getFloat();
				int minPtr = minPtr_pan.getInt();
				long sleep = sleep_pan.getInt();
				
				(new Thread(new DBSCAN_Rtree(list, eps, minPtr, sleep))).start();
				
				dispose();
			}
		});
		
	}

	private static final long serialVersionUID = 157868856594840650L;

}
