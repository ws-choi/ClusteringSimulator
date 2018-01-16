package com.clustering.dbscan.gui;

import gui.Infinit_repainter;
import gui.communication.CWS_socket;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class VisualizedData extends JPanel {

	private static final long serialVersionUID = -4050902797485052023L;

	CWS_socket visual_to_data;
	
	LinkedList<DataTuple> datalist;
	
	public VisualizedData(LinkedList<DataTuple> datalist) {
	
		this.datalist = datalist;
		setBackground(Color.WHITE);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));


		
		(new Thread(new Infinit_repainter(this))).start();


	}

	
	@Override
	public void paint(Graphics g) {
		
	
		if(datalist!=null)	{

			super.paint(g);
			print_Data(g);
			
		}
		
	}
	
	private void print_Data(Graphics g){
		
		DataTuple[] array = datalist.toArray(new DataTuple[0]);
		
		for (DataTuple tuple : array) {
			g.setColor(get_hash_color(tuple));
			
			if(tuple.isSpecial())
			{
				g.drawString("Seed", (int) tuple.data[0]-15, (int)tuple.data[2]);
				g.drawRoundRect((int) tuple.data[0]-30, (int)tuple.data[2]-30,
						60, 60, 60, 60);
			}
			
			else if(tuple.isNoise())
				g.drawString("X", (int) tuple.data[0], (int)tuple.data[2]);

			else if(tuple.isUnClassified())
				g.fillRoundRect((int) tuple.data[0], (int)tuple.data[2],10,10,10,10);
			
			else if((tuple.cluster_id/5)%2==0)
				g.fillRoundRect((int) tuple.data[0], (int)tuple.data[2],10,10,10,10);
			
			else 
				g.drawRoundRect((int) tuple.data[0], (int)tuple.data[2],10,10,10,10);


		}		
		
		array = null;
		
		System.gc();
	
	}
	
	
	private Color get_hash_color (DataTuple tuple){
		
		if(tuple.isUnClassified())
			return Color.gray;
	
		if(tuple.isNoise())
			return Color.black;
		
		int hash = tuple.cluster_id%5;
		
		switch (hash) {
		case 0:
			return Color.RED;
		case 1:
			return Color.ORANGE;
		case 2:
			return Color.BLUE;
		case 3:
			return Color.GREEN;
		case 4:
			return Color.PINK;
		default:
			break;
		}
		
		return Color.YELLOW;
	}
	
	


}
