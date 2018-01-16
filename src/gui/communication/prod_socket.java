package gui.communication;

import gui.event.My_Event;

import java.util.LinkedList;


public class prod_socket extends Thread  {

	LinkedList<My_Event> Queue;
	CWS_consumer cons;
	int port;
	
	public prod_socket(CWS_consumer consumer, int port) {
		Queue = new LinkedList<My_Event>();
		cons = consumer;
		this.port = port;
	}
		
	@Override
	public void run() {
		
		wait_producer();
		
	}
	
	public synchronized void notify_event (My_Event event){
		Queue.add(event);
		notifyAll();
		
	}
	
	public synchronized void wait_producer (){
		
		while (true) {
			try {
				wait();
				while(!Queue.isEmpty())
					cons.consume_something(port, Queue.remove());
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}


