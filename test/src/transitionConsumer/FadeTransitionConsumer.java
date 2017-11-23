package transitionConsumer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.Duration;


/**
 * A FadeTransition-Consumer for Node preview.  <br>
 * Delay before starting a FadeTransition defaults to 250 milliseconds. <br>
 * Add ToggleButtons & Nodes and choose witch node to show initially by calling setVisible(...) at start.  <br> <br>
 *
 * Methods:  <br>
 * setDelay(), setDuration(), setPreviewOpacity(),  add(), changePreview(), setSelected() <br>
 * @author gergang
 *  
 */
public class FadeTransitionConsumer {
	private ConcurrentLinkedQueue  <ExtendedFadeTransition> q = new ConcurrentLinkedQueue <ExtendedFadeTransition>();
	private HashMap<Node, ExtendedFadeTransition> hm = new HashMap<Node, ExtendedFadeTransition>();
	private HashMap<ToggleButton, Boolean> hmAllowPreview = new HashMap<ToggleButton, Boolean>();
	private Object lock = new Object();
	private Lock innerLock = new Lock();
	private Lock outerLock = new Lock();
	private Lock delayLock = new Lock();
	private boolean stop=false;
	private long delayMs=300; //milliseconds before a transition will play
	private Duration duration=new Duration(150); //transition duration
	private ToggleGroup tg = new ToggleGroup();
	private Node currentNode=null;
	private double previewOpacity=0.7;
	private Label dummyNode=new Label();

	public FadeTransitionConsumer (){
		Runnable task = () -> {
			while (!stop){
				while(!q.isEmpty() && q.size()>1){
					synchronized (lock){ //make sure not to remove an element while adding one
						play(q.remove());
						play(q.remove());
					}
					innerLock.hold();
					if(stop) break;
				}
				if(stop) break;
				outerLock.hold();
			}
			System.out.println("thread ended");
		};	
		new Thread(task).start();
	}

	private void play(ExtendedFadeTransition eft){
		FadeTransition ft = eft.transition;
		Node node = ft.getNode();
		hm.remove(node);
		if(eft.fadeIn){
			setVisible(node, true);
			ft.setOnFinished(e-> innerLock.release());
		} else{
			ft.setOnFinished(e-> {
				innerLock.release();
				setVisible(node, false);
			});
		}
		ft.play();
	}
	
	private void setVisible(Node node, boolean b){
		Platform.runLater(() -> {
			if(b) node.setOpacity(0);
			node.setVisible(b);
		});
	}
	
	/**
	 * Ensure that an unintentional mouse-entered or -exited event will be canceled before it plays.<br>
	 * This needs to run in a thread outside the synchronized(lock) method obviously
	 */
	private void delay(){ 
		Runnable task = () -> {
			delayLock.hold(delayMs);
			outerLock.release();
		};	
		new Thread(task).start();
	}





	private void show(final Node node){
		if(node == currentNode) return;
		synchronized (lock){
			if(!isWaitingOnQueue(currentNode))	addTransitionDelayed(duration, currentNode, false, currentNode.getOpacity(), 0);
			if(!isWaitingOnQueue(node))			addTransitionDelayed(duration, node, true, 0, previewOpacity);
		}
	}
	private synchronized void unShow(final Node node){
		synchronized (lock){
			if(!isWaitingOnQueue(node))			addTransitionDelayed(duration, node,  false,previewOpacity, 0);
			if(!isWaitingOnQueue(currentNode))	addTransitionDelayed(duration, currentNode,  true, 0, 1);
		}
	}
	private void addTransitionDelayed (Duration duration, Node node,  boolean in, double fromValue ,double toValue){
		q.add(new ExtendedFadeTransition(duration, node, in, fromValue, toValue));
		if(delayMs>0) delay(); else outerLock.release();
	}

	private void select(Node node){
		synchronized (lock){
			q.add(new ExtendedFadeTransition(new Duration(50), currentNode, true, currentNode.getOpacity(), 0));
			currentNode=node;
			q.add(new ExtendedFadeTransition(new Duration(50), currentNode, true, previewOpacity, 1.0)); //twice because need to 
			Platform.runLater( () ->node.toFront() );
			outerLock.release();
		}
	}
	
	/** cancel out a node if its counterpart is still on the queue */
	private boolean isWaitingOnQueue(Node node){
		if(hm.containsKey(node)){
			//prt("rmv:" + ((Label)node).getText() + " = true");
			q.remove(hm.get(node));
			hm.remove(node);
			return true;
		}
		return false;
	}

	
	
	
	
	
	
	
	
	/************************** PUBLIC  METHODS  *******************************/

	public  void stop() {
		stop=true;
		delayLock.release();
		innerLock.release();
		outerLock.release();
	}


	/**
	 * 
	 * @param ms : Grace period before fade transitions start. Default is 300 milliseconds.
	 */
	public void setDelay(long ms) {
		this.delayMs = ms;
	}

	/**
	 * 
	 * @param ms : Transition duration; default is 150 milliseconds. FadeIn & FadeOut play simultaneously
	 */
	public void setDuration(long ms) {
		this.duration = new Duration(ms);
	}

	/**
	 * 
	 * @param Preview opacity : default is 0.7
	 */
	public void setPreviewOpacity(double previewOpacity){
		this.previewOpacity=previewOpacity;
	}

	/**
	 * 
	 * @param toggleButton  
	 * @param nodeToShowOrSelect : The node that will become visible (fade in) at mouse over (or when clicked)
	 */
	public void add(final ToggleButton toggleButton, final Node nodeToShowOrSelect, boolean allowPreview){
		nodeToShowOrSelect.setOpacity(0);
		nodeToShowOrSelect.setVisible(false);
		tg.getToggles().add(toggleButton);
		toggleButton.setOnMouseEntered(e-> 	{if(hmAllowPreview.get(toggleButton)) show(nodeToShowOrSelect);}) ;
		toggleButton.setOnMouseExited(e->		{if(hmAllowPreview.get(toggleButton)) unShow(nodeToShowOrSelect);}) ;
		toggleButton.setOnMouseClicked(e-> 	{
			if(!toggleButton.isSelected()) toggleButton.setSelected(true);
			else select(nodeToShowOrSelect);
		}) ;
		hmAllowPreview.put(toggleButton, new Boolean(allowPreview));
	}

	public void changePreview(ToggleButton tb, boolean allowPreview){
		hmAllowPreview.put(tb, new Boolean(allowPreview));
	}
	public void setSelected(ToggleButton tb, final Node node){
		tb.setSelected(true);
		Platform.runLater( () ->node.toFront() );
		node.setVisible(true);
		node.setOpacity(1);
		currentNode=node;
	}

	
/************************** INTERNAL CLASSES  *******************************/

	class ExtendedFadeTransition{
		FadeTransition transition;
		boolean fadeIn;
		public ExtendedFadeTransition(Duration duration, final Node node, boolean prepareFadeIn, double fromValue , double toValue){
			this.transition=new FadeTransition(duration, node);
			transition.setFromValue(fromValue);
			transition.setToValue(toValue);
			transition.setCycleCount(1);
			this.fadeIn=prepareFadeIn;
			hm.put(node, this);
		}
	}
	

}
