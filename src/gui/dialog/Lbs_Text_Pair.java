package gui.dialog;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Lbs_Text_Pair extends JPanel{
	
	private static final long serialVersionUID = -3626179699562023978L;
	JLabel lb;
	JTextField text;
	
	public Lbs_Text_Pair(String lb_name, int col) {
		
		lb = new JLabel(lb_name);
		text = new JTextField(col);
		
		setLayout(new FlowLayout());
		
		add(lb);
		add(text);

	}
	
	public JTextField get_text_field () { return text;}

	public int getInt() throws NumberFormatException {
		return Integer.parseInt(text.getText());
	}
	
	public float getFloat () throws NumberFormatException{
		return Float.parseFloat(text.getText());
	}
	
}
