package gui.dialog;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class My_Dialog extends JDialog{

	private static final long serialVersionUID = 1241429689438918825L;
	protected JButton btn;
	public My_Dialog(JFrame gui, String top_msg, String first_msg) {
		super(gui, top_msg);
		
		JLabel msg_north = new JLabel(first_msg);
		add(msg_north, BorderLayout.NORTH);

		btn = new JButton("OK");
		
		add(btn, BorderLayout.SOUTH);
		setSize(300, 100);
		setVisible(true);
	}
}
