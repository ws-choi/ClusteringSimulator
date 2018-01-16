package gui.communication;

import gui.event.My_Event;

public interface CWS_communication {
	
	public void produce_somthing (int port, My_Event my_Evnet);
	public void consume_something (int port, My_Event my_Event);
	public void set_socket(int port, CWS_socket cws_socket);
	
}
