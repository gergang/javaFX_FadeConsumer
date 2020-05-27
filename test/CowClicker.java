package treeTest;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Indentation is lost in JavaFX 11 & 14
 */

public class CowClicker extends Application {
	static int mode = 0;

	ArrayList<ScheduledExecutorService> executorThreads = new ArrayList<ScheduledExecutorService>();
	private TreeItem<Cow> root = new TreeItem<Cow>(new Cow("Color"));
	private DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm:ss");

	/**
	 * modes:
	 * 0 -> foodColumn first;  this works fine in JavaFX 11 & 14
	 * 1 -> checkboxes unlabeled first; 
	 * 2 -> checkboxes labeled & in first column
	 */
	public static void main(String[] args) {
		if(args.length==1) {
			try{
				mode = Integer.parseInt(args[0]);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(mode>2) {
			mode=0;
			System.out.println("mode 0,1 or 2. Changed to mode 0.");
		}
		Application.launch(CowClicker.class, args);
	}


	@Override
	public void start(Stage stage) {

		TreeTableColumn<Cow, Control> rbColumn = new TreeTableColumn<>("Click here");
		rbColumn.setCellValueFactory(p -> p.getValue().getValue().checkedProperty());
		rbColumn.setCellFactory(p-> new FxControls_Cell());

		TreeTableColumn<Cow, String> cowColorColumn = new TreeTableColumn<>("Cows");
		cowColorColumn.setCellValueFactory(p -> p.getValue().getValue().cowProperty());

		TreeTableColumn<Cow, String> countDownColumn = new TreeTableColumn<>("Wait");
		countDownColumn.setCellValueFactory(p -> p.getValue().getValue().disabledTimeProperty());


		TreeTableView<Cow> ttv  = new TreeTableView<Cow>(root);
		cowColorColumn.setPrefWidth(90);
		countDownColumn.setPrefWidth(55);
		if(mode==2) rbColumn.setPrefWidth(120);
		else rbColumn.setPrefWidth(80);

		switch (mode){
		case 0: ttv.getColumns().setAll(cowColorColumn, rbColumn, countDownColumn);break;
		case 1: ttv.getColumns().setAll(rbColumn, cowColorColumn,  countDownColumn); break;
		case 2: ttv.getColumns().setAll(rbColumn, countDownColumn);break;
		}
		ttv.setShowRoot(true);
		root.setExpanded(true);

		createData();

		VBox vb = new VBox();
		Label l1=new Label("Java Version:\t" + System.getProperties().get( "java.version"));
		Label l2=new Label("JavaFX Version:\t" + System.getProperties().get("javafx.runtime.version"));
		Label l3=new Label();
		//l3.setMaxHeight(10);
		vb.getChildren().addAll(l1,l2,l3,ttv);

		Scene scene = new Scene(vb);
		stage.setHeight(250);
		stage.setOnCloseRequest((ev)-> executorThreads.forEach(ex->ex.shutdownNow()));
		stage.setScene(scene);
		stage.show();
	}


	// customized tree table cell
	class FxControls_Cell extends TreeTableCell<Cow, Control> {
		@Override 
		public void updateItem(Control item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {   
				setText(null);
				setGraphic(null);
			} else {
				setGraphic(item);
			}
		}
	}


	// Data section
	enum Section{
		NULL, COW
	}
	private void createData() {addItems("White", "Black", "Piebald", "Buff");}
	private void addItems(String ... sa) {
		for (int i=0; i<sa.length;i++){
			root.getChildren().add(new TreeItem<Cow>(new Cow(sa[i], true)));
		}
	}

	public class Cow {
		private RadioButton b = new RadioButton();
		private boolean clickable=false;

		private Cow(String s) {
			this(s,false);
		}
		private Cow(String s, boolean clickable) {
			this.clickable=clickable;
			cow  = new SimpleStringProperty(s);
			disabledTime = new SimpleObjectProperty<String> ("");
			checked = new SimpleObjectProperty<Control>(b);
			if(mode==2) b.setText(s);
			b.setOnMouseClicked(a->	countDown());
			if(!clickable) b.setDisable(true);
			
		}

		//properties, getters & setters 
		private SimpleObjectProperty<Control> checked;
		public SimpleObjectProperty<Control> checkedProperty(){ return checked;}
		public Control getChecked() {	return checked.get();}
		public void setChecked(Control c)	{ Platform.runLater(() -> checked.set(c)); }

		private SimpleStringProperty cow;
		public SimpleStringProperty cowProperty() { return cow; }
		public String getCow() {return cow.get();}
		public void setCow(String s) 	{ Platform.runLater( () -> cow.set(s)  ); }

		private SimpleObjectProperty<String> disabledTime;
		public SimpleObjectProperty<String> disabledTimeProperty(){ return disabledTime;}
		public String getDisabledTime() {return disabledTime.get();}
		public void setDisabledTime(String s)	{ Platform.runLater(() -> disabledTime.set(s)); }

		//other
		public void countDown() {
			if(!clickable) return;
			System.out.println("Congratulations - you clicked a "+ cow.get().toLowerCase() + " cow! Now wait 6 hours before doing this again.");
			Platform.runLater( () -> b.setDisable(true));
			AtomicInteger a = new AtomicInteger(21600);
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
			executorThreads.add(executor);
			
			Runnable r = ()->{
				int i = a.decrementAndGet();
				if(i<1 ){
					executor.shutdown();
					executorThreads.remove(executor);
					Platform.runLater( () ->{ 
						b.setDisable(false);
						b.setSelected(false);
					});
					setDisabledTime("");
				}else {
					setDisabledTime(LocalTime.ofSecondOfDay(i).format(HH_MM));
				}
			};
			executor.scheduleAtFixedRate(r, 1, 1, TimeUnit.SECONDS);
		}
	}

}
