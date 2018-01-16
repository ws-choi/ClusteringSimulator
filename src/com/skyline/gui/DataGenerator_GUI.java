package com.skyline.gui;

import gui.communication.CWS_consumer;
import gui.communication.CWS_producer;
import gui.communication.prod_socket;
import gui.event.My_Event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.skyline.global.MyConstants;

import data.generator.Anti_CorrDG;
import data.generator.NormalDG;
import data.generator.UniformDG;

public class DataGenerator_GUI extends JPanel implements CWS_producer, CWS_consumer{
	
	private static final long serialVersionUID = 382433424330867546L;
	SingleWindow_UI frame;
	prod_socket DG_TO_SINGLE, SINGLE_TO_DG;
	JCheckBox chckbxFixedPrice ;
	
	int index;
	public DataGenerator_GUI( SingleWindow_UI frame ) {
	
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
				DG_TO_SINGLE.notify_event(null);
			}
		});
		
		chckbxFixedPrice = new JCheckBox("Fixed Price");
		add(chckbxFixedPrice);
		add(btnGenerateInsert);
	}


	@Override
	public void consume_something(int port, My_Event my_Event) {
		
		switch (port) {
		case MyConstants.std:
			
			if(my_Event instanceof BoundsInfo)
			{
				BoundsInfo info = (BoundsInfo) my_Event;
				My_Event result = new My_Event();
				try {
					
					if(chckbxFixedPrice.isSelected()) info.bounds[2][0]=info.bounds[2][1]=200;
					
					switch (index) {
					case 0:
						result.carrier = new UniformDG(info.dim, info.bounds, info.floatEnable);						
						break;
						
					case 1:
						result.carrier = new NormalDG(info.dim, info.bounds, info.floatEnable);
						break;
						
					case 2:
						result.carrier = new Anti_CorrDG(info.dim, info.bounds, info.floatEnable, 0, 1);
						break;
						

					default:
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				DG_TO_SINGLE.notify_event(result);
			}
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void set_socket(int port, prod_socket cws_socket) {

		switch (port) {
		case MyConstants.dts:
			DG_TO_SINGLE = cws_socket;			
			break;
			
		case MyConstants.std:
			SINGLE_TO_DG = cws_socket;

		default:
			break;
		}		
	}

}
