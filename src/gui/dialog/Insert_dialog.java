package gui.dialog;

import gui.event.My_Event;

import index.rtree.query.skyline.hotel.HtS_Event;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;


import com.skyline.global.MyConstants;
import com.skyline.gui.Hotel_Area;

public class Insert_dialog extends My_Dialog
{
	private static final long serialVersionUID = -6830650040754463711L;
	JTextField price_field;
	float price;
	Point cursor_point;
	Hotel_Area harea;
	
	public Insert_dialog(Hotel_Area area, Point point) {
		
		super(area.gui, "Hotel price", "Please select price in [100, 500]");
		this.cursor_point=point;
		this.harea= area;
		
		JLabel msg_west= new JLabel("price: ");
		add(msg_west, BorderLayout.WEST);

		price_field = new JTextField();
		add(price_field);
		
		btn.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if( validation(price_field.getText()) )
				{
					My_Event event=new HtS_Event(price, cursor_point);
					harea.produce_somthing(MyConstants.hts, event);
					dispose();
				}
				
				else
					price_field.setText("Please select a real value in [100, 500]");
				
			}
		});
		
	}
	
	public boolean validation ( String text ){
	
		try {
			price = Float.parseFloat(text);
			
			if(MyConstants.price_bounds[0] <= price)
				if(MyConstants.price_bounds[1] >= price)
					return true;
			
			return false;
			
		} catch (NumberFormatException e) {
			return false;
		}

	}
}