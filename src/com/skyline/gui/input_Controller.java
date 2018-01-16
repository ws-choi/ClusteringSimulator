package com.skyline.gui;

import gui.communication.CWS_producer;
import gui.communication.prod_socket;
import gui.event.My_Event;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import com.skyline.global.MyConstants;

public class input_Controller extends JPanel implements CWS_producer {

	private static final long serialVersionUID = -8653202349642060381L;
	
	prod_socket this_to_Hotel;
	Hotel_Area hotel_area;
	JCheckBox chckbxHotelexample;
	JRadioButton rdbtnSubmitSkylineQuery;
	
	public input_Controller() {
	
		init();
		
	}

	private void init() {
		setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[] {0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0};
		setLayout(gridBagLayout);
		
		rdbtnSubmitSkylineQuery = new JRadioButton("Submit a skyline Query");
		
		rdbtnSubmitSkylineQuery.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
		
				My_Event event = new My_Event();
				
				if(chckbxHotelexample.isSelected())
					event.carrier = new Integer(MyConstants.hotel_skyline);
				else
					event.carrier = new Integer(MyConstants.normal_skyline);
					
				this_to_Hotel.notify_event(event);
				
			}
		});
		
		GridBagConstraints gbc_rdbtnSubmitSkylineQuery = new GridBagConstraints();
		gbc_rdbtnSubmitSkylineQuery.anchor = GridBagConstraints.EAST;
		gbc_rdbtnSubmitSkylineQuery.weightx = 1.0;
		gbc_rdbtnSubmitSkylineQuery.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnSubmitSkylineQuery.gridx = 1;
		gbc_rdbtnSubmitSkylineQuery.gridy = 0;
		add(rdbtnSubmitSkylineQuery, gbc_rdbtnSubmitSkylineQuery);
		
		JRadioButton rdbtnInsertAHotel = new JRadioButton("Insert a hotel");
		
		rdbtnInsertAHotel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
		
				My_Event event = new My_Event();
				event.carrier = new Integer(MyConstants.insert);
				this_to_Hotel.notify_event(event);
				
			}
		});
		
		GridBagConstraints gbc_rdbtnInsertAHotel = new GridBagConstraints();
		gbc_rdbtnInsertAHotel.anchor = GridBagConstraints.EAST;
		gbc_rdbtnInsertAHotel.weightx = 1.0;
		gbc_rdbtnInsertAHotel.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnInsertAHotel.gridx = 0;
		gbc_rdbtnInsertAHotel.gridy = 0;
		add(rdbtnInsertAHotel, gbc_rdbtnInsertAHotel);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnInsertAHotel);
		group.add(rdbtnSubmitSkylineQuery);
		
		chckbxHotelexample = new JCheckBox("Hotel_example");
		chckbxHotelexample.setSelected(true);
		GridBagConstraints gbc_chckbxHotelexample = new GridBagConstraints();
		gbc_chckbxHotelexample.gridx = 2;
		gbc_chckbxHotelexample.gridy = 0;
		add(chckbxHotelexample, gbc_chckbxHotelexample);
		
		chckbxHotelexample.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(rdbtnSubmitSkylineQuery.isSelected())
				{
					My_Event event = new My_Event();
					
					if(chckbxHotelexample.isSelected())
						event.carrier = new Integer(MyConstants.hotel_skyline);
					else
						event.carrier = new Integer(MyConstants.normal_skyline);
						
					this_to_Hotel.notify_event(event);
				}
			}
		});
		
		rdbtnSubmitSkylineQuery.setSelected(true);
		
	}


	@Override
	public void set_socket(int port, prod_socket cws_socket) {
		switch (port) {
		case MyConstants.ith:
			this_to_Hotel = cws_socket;
			
			break;

		default:
			break;
		}
	}

	
}
