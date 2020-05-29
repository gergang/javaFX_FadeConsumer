package transitionConsumer;

/**
 * A better alternative to Thread.sleep(millis).<br>
 * Call to release() when exiting (for instance with System.exit()) to notify any waiting thread.<br>
 * No multi threading, just one lock.<br><br>
 * Methods:<br>
 * hold(), hold(millis), release()
 * @author ge
 */
public class Lock {

	private Object  waitObject = new Object();

	/**
	 * canceled by release() or timeOut
	 * @param ms Milliseconds until timeout
	 */
	public  void hold(long ms){
		synchronized (waitObject){
			try {waitObject.wait(ms);} catch (InterruptedException e) {} 
		}
	}
	
	/**
	 * canceled by release()
	 */
	public  void hold(){
		synchronized (waitObject){
			try {waitObject.wait();} catch (InterruptedException e) {} 
		}
	}
	public  void release(){
		synchronized (waitObject){
			try{waitObject.notify();} catch(IllegalMonitorStateException e){e.printStackTrace();}
		}
	}
}
