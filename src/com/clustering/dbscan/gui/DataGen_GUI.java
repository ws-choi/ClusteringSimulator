package com.clustering.dbscan.gui;

import gui.communication.CWS_communication;
import gui.communication.CWS_socket;
import gui.event.My_Event;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import data.generator.Anti_CorrDG;
import data.generator.NormalDG;
import data.generator.UniformDG;

public class DataGen_GUI extends JPanel implements CWS_communication{
	
	private static final long serialVersionUID = 382433424330867546L;

	ClusteringWindow frame;
	
	CWS_socket data_to_window;
	

	
	int index;
	public DataGen_GUI( ClusteringWindow frame ) {
	
		this.frame = frame;
		
		JLabel lblSelectDistribution = new JLabel("Select Distribution");
		add(lblSelectDistribution);
		
		final JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Uniform Distribution", "Normal Distribution", "Antri-Correlated Distribution"}));
		add(comboBox);
		
		JButton btnGenerateInsert = new JButton("Generate & Insert!");
		btnGenerateInsert.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				index = comboBox.getSelectedIndex();
				data_to_window.notify_event(null);
			}
		});
		
		add(btnGenerateInsert);
	}

	@Override
	public void produce_somthing(int port, My_Event my_Evnet) {
		
	}



	@Override
	public void consume_something(int port, My_Event my_Event) {
		
		switch (port) {
		case Constants.window_to_data:
			
			if(my_Event instanceof Size_Event)
			{
				My_Event result = new My_Event();
				
				Dimension dim = ((Size_Event)my_Event).dim;

				System.out.println(dim);
				float[][] bounds = { {0,0}, {0,0} };

				bounds[0][1] = dim.width;
				bounds[1][1] = dim.height;
				
				try {
					
					switch (index) {
					case 0:
						result.carrier = new UniformDG(2, bounds, false);						
						break;
						
					case 1:
						result.carrier = new NormalDG(2, bounds, false);
						break;
						
					case 2:
						result.carrier = new Anti_CorrDG(2, bounds, false, 0, 1);
						break;
						

					default:
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				data_to_window.notify_event(result);
			}
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void set_socket(int port, CWS_socket cws_socket) {
		
		switch (port) {

		case Constants.data_to_window:
		
			data_to_window=cws_socket;
		default:
			break;
		}
	}

}
