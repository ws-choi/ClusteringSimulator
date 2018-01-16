package gui;

import java.util.concurrent.Semaphore;

import javax.swing.JPanel;

public class Infinit_repainter implements Runnable {

	Semaphore sem = new Semaphore(1);
	JPanel panel;
	public Infinit_repainter(JPanel panel) {
		this.panel = panel;
	}
	@Override
	public void run() {
		while(true){
			
			if(sem.tryAcquire()){
				panel.repaint();
				sem.release();	
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
