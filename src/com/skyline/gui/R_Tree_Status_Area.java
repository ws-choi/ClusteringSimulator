package com.skyline.gui;

import gui.communication.CWS_producer;
import gui.communication.prod_socket;
import gui.event.My_Event;
import index.rtree.dimitris.RTree;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.skyline.global.MyConstants;

public class R_Tree_Status_Area extends JPanel implements CWS_producer {
	
	public RTree rtree;
	prod_socket R_TO_SINGLE, R_TO_HOTEL;
	
	private static final long serialVersionUID = -138478784876983836L;
	public JTextField num_of_data_field;
	public JTextField tree_height_field;
	public R_Tree_Status_Area(RTree tree) {
		
		this.rtree=tree;
		
		init();
		
		Thread checker = new Thread(new infinit_checker(this));
		
		JButton btnDestroyCreate = new JButton("Clear");
		btnDestroyCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				rtree = new RTree("sample.r", 256, 0, 3);
				My_Event event = new My_Event();
				event.carrier = rtree;
				
				R_TO_HOTEL.notify_event(event);
				R_TO_SINGLE.notify_event(event);
				
			}
		});
		btnDestroyCreate.setFont(new Font("±¼¸²", Font.BOLD, 12));
		GridBagConstraints gbc_btnDestroyCreate = new GridBagConstraints();
		gbc_btnDestroyCreate.fill = GridBagConstraints.BOTH;
		gbc_btnDestroyCreate.gridwidth = 2;
		gbc_btnDestroyCreate.insets = new Insets(0, 0, 5, 5);
		gbc_btnDestroyCreate.gridx = 0;
		gbc_btnDestroyCreate.gridy = 1;
		add(btnDestroyCreate, gbc_btnDestroyCreate);
		
		JLabel lblOfData = new JLabel("# of Data");
		lblOfData.setFont(new Font("±¼¸²", Font.BOLD, 12));
		GridBagConstraints gbc_lblOfData = new GridBagConstraints();
		gbc_lblOfData.weighty = 1.0;
		gbc_lblOfData.weightx = 1.0;
		gbc_lblOfData.anchor = GridBagConstraints.EAST;
		gbc_lblOfData.insets = new Insets(0, 0, 5, 5);
		gbc_lblOfData.gridx = 0;
		gbc_lblOfData.gridy = 2;
		add(lblOfData, gbc_lblOfData);
		
		num_of_data_field = new JTextField();
		GridBagConstraints gbc_num_of_data_field = new GridBagConstraints();
		gbc_num_of_data_field.insets = new Insets(0, 0, 5, 0);
		gbc_num_of_data_field.weighty = 1.0;
		gbc_num_of_data_field.weightx = 1.0;
		gbc_num_of_data_field.fill = GridBagConstraints.HORIZONTAL;
		gbc_num_of_data_field.gridx = 1;
		gbc_num_of_data_field.gridy = 2;
		add(num_of_data_field, gbc_num_of_data_field);
		num_of_data_field.setColumns(5);
		
		JLabel lblHeightOfTree = new JLabel("Height of Tree");
		lblHeightOfTree.setFont(new Font("±¼¸²", Font.BOLD, 12));
		GridBagConstraints gbc_lblHeightOfTree = new GridBagConstraints();
		gbc_lblHeightOfTree.weighty = 1.0;
		gbc_lblHeightOfTree.weightx = 1.0;
		gbc_lblHeightOfTree.anchor = GridBagConstraints.EAST;
		gbc_lblHeightOfTree.fill = GridBagConstraints.VERTICAL;
		gbc_lblHeightOfTree.insets = new Insets(0, 0, 0, 5);
		gbc_lblHeightOfTree.gridx = 0;
		gbc_lblHeightOfTree.gridy = 3;
		add(lblHeightOfTree, gbc_lblHeightOfTree);
		
		tree_height_field = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 3;
		add(tree_height_field, gbc_textField);
		tree_height_field.setColumns(5);
		checker.start();
		
	}
	private void init() {
		
		setBackground(Color.WHITE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE, 0.0};
		setLayout(gridBagLayout);
		
		JLabel lblRTreeStatus = new JLabel("R Tree Status");
		lblRTreeStatus.setBackground(Color.WHITE);
		lblRTreeStatus.setFont(new Font("±¼¸²", Font.BOLD, 20));
		GridBagConstraints gbc_lblRTreeStatus = new GridBagConstraints();
		gbc_lblRTreeStatus.weighty = 1.0;
		gbc_lblRTreeStatus.insets = new Insets(0, 0, 5, 0);
		gbc_lblRTreeStatus.fill = GridBagConstraints.VERTICAL;
		gbc_lblRTreeStatus.gridwidth = 2;
		gbc_lblRTreeStatus.gridx = 0;
		gbc_lblRTreeStatus.gridy = 0;
		add(lblRTreeStatus, gbc_lblRTreeStatus);
		
	}

	@Override
	public void set_socket(int port, prod_socket cws_socket) {

		switch (port) {
		case MyConstants.rth:
			R_TO_HOTEL = cws_socket;
			break;
			
		case MyConstants.rts:
			R_TO_SINGLE = cws_socket;
			break;

		default:
			break;
		}		
	}
	
}

class infinit_checker implements Runnable {
	
	R_Tree_Status_Area rsa;
	
	public infinit_checker(R_Tree_Status_Area rsa) {
		this.rsa = rsa;
	}
	@Override
	public void run() {
		
		while(true){
			rsa.num_of_data_field.setText(rsa.rtree.get_num()+"");
			rsa.tree_height_field.setText(rsa.rtree.root_ptr.level+"");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
