import java.util.logging.Level;
import java.util.logging.Logger;

public class TableThread extends Thread{
	private final int timer;
	private final int ttl;

    /**
     * Constructor for the TableThread object.
     *
     * @param timer This is the refresh rate for the table.
     * @param ttl This is the maximum time to live for each NAT table entry
     */
    public TableThread(int timer, int ttl) {
        this.timer = timer;
    	this.ttl = ttl;
    }
    
    /**
     * This starts the TableThread which removes unused entries from the
     * NAT table, either by means of ttl or from clients that have disconnected.
     * This check is done on a timer, specified when creating the TableThread
     * object.
     */
    @Override
    public void run() {
        boolean stillRunning = true;

        while (stillRunning) {
            try {
                //Dynamically update NAT Table based on configured timer.
                for (int i = 0; i < NATRouter.natTable.size(); i++) {
                    boolean remove = true;
                    for (int j = 0; j < NATRouter.clientList.size(); j++) {
                        if (NATRouter.clientList.get(j).getIP().equalsIgnoreCase(NATRouter.natTable.get(i).getInternalIp())) { //if client ip == table entry ip
                            remove = false;
                            break;
                        }
                    }
					
					if ((System.nanoTime() - NATRouter.natTable.get(i).getTimestamp())/1000000 > ttl) {
                            remove = true;
                    }
					
                    if (remove) {
                        NATRouter.natTable.remove(i);
                    }
                }  
                
				NATRouter.printNatTable();
                Thread.sleep(timer);
            } catch (InterruptedException ex) {
                Logger.getLogger(TableThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        close();
	}
	
    /**
     * This method closes the TableThread thread.
     */
    public void close() {
    	//Close Thread
	}
}
