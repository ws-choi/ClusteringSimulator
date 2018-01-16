package gui.communication;

import gui.event.My_Event;

public interface CWS_consumer {
	public void consume_something (int port, My_Event my_Event);
	
}
