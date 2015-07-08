package fcmps.ui;

public class DeadLock implements Runnable{
	private Thread th;
	
	boolean isStop=false;
	public DeadLock() {
		th=new Thread(this);
		th.start();
	}
	public void run() {
        // Now find deadlock
        ThreadMonitor monitor = new ThreadMonitor();
        boolean found = false;
        
        while (!found) {
        	if(isStop) break;
            found = monitor.findDeadlock();
            try {
                th.sleep(500);
            } catch (InterruptedException e) {
            	e.printStackTrace();
                System.exit(1);
            }
        }
        th.interrupt();
        th=null;
	}
	
	public void doStop() {
		isStop=true;
	}	
	
}
