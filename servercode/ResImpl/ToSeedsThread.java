package ResImpl;

import java.util.concurrent.ConcurrentLinkedQueue;

import Commands.Command;

public class ToSeedsThread extends Thread {
    
    private ConcurrentLinkedQueue<Command> clq;

    public ToSeedsThread(ConcurrentLinkedQueue<Command> pclq) {
	clq = pclq;
    }

    public void run() {
	Command c;
	while (true) {
	    c = clq.poll();
	    if (c != null) {
		System.out.println("Executing command.");
		c.execute();
	    }
	    else
		Thread.yield();
	}
    }
}
