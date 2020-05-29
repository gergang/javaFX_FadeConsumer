package treeTest;

import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * CheckBoxTreeTableCell
 */
public class VendingMachine  extends Application{
	private TreeItem<FoodItem> root = new TreeItem<>(new FoodItem(Section.NULL));

	public static void main(String[] args) {
		Application.launch(VendingMachine.class, args);
	}
	@Override
	public void start(Stage stage) {
		root.setExpanded(true);
		
		TreeTableView<FoodItem> ttv=new TreeTableView<>(root);

		TreeTableColumn<FoodItem,Boolean> checkBoxColumn=new TreeTableColumn<>("Check");
		checkBoxColumn.setCellValueFactory(p -> p.getValue().getValue().checkedProperty());
		checkBoxColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(checkBoxColumn)); //render boolean value as CheckBoxTreeTableCell
		checkBoxColumn.setPrefWidth(70);
		

		TreeTableColumn<FoodItem,String> nameColumn=new TreeTableColumn<>("Item");
		nameColumn.setCellValueFactory(p -> p.getValue().getValue().itemProperty());
		nameColumn.setPrefWidth(100);
		
		TreeTableColumn<FoodItem, String> stockColumn = new TreeTableColumn<>("Stock");
		stockColumn.setCellValueFactory(p -> p.getValue().getValue().stockProperty());
		stockColumn.setCellFactory(p-> new Stock_Cell());	//Customized label
		
		createData();

		boolean checkboxesFirst=true;
		if(checkboxesFirst) {
			ttv.getColumns().addAll(checkBoxColumn, nameColumn, stockColumn);
		}else {
			ttv.getColumns().addAll( nameColumn, checkBoxColumn, stockColumn);
		}
		
		
		ttv.setEditable(true);
		ttv.setShowRoot(false);

		VBox vb = new VBox();
		Label l1=new Label("Java Version:\t" + System.getProperties().get( "java.version"));
		Label l2=new Label("JavaFX Version:\t" + System.getProperties().get("javafx.runtime.version"));
		vb.getChildren().addAll(l1,l2,new Label(),ttv);
		
		Scene scene = new Scene(vb);
		stage.setHeight(380);
		stage.setScene(scene);
		stage.show();
	}

	class Stock_Cell extends TreeTableCell<FoodItem, String> {
		@Override 
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
				setGraphic(null);
			}  else {
				Label l = new Label(item);
				l.setRotate(15.0);
				l.setMaxHeight(50);
				l.setStyle("-fx-font-weight:bold;-fx-font-size:12px");
				setGraphic(l);
			}
		}
	}
	
	//DATA SECTION
	enum Section{
		NULL, LIQUIDS, FOODS, SURVIVAL
	}
	
	private void createData() {
		addItems(Section.LIQUIDS, "Water", "Sparkling Water", "Coffee, black");
		addItems(Section.FOODS, "Cheese Sandwich", "Ham Sandwich" , "Ham & Cheese Sandwich");
		addItems(Section.SURVIVAL, "Beer", "Med Kit");
	}
	
	private void addItems(Section s, String ... sa) {
		final TreeItem<FoodItem> foods = new TreeItem<FoodItem>(new FoodItem(s));
		root.getChildren().add(foods);
		for (int i=0; i<sa.length;i++){
			foods.getChildren().add(new TreeItem<FoodItem>(new FoodItem(s, sa[i])));
		}
		foods.setExpanded(true);
	}

	class FoodItem {
		private FoodItem(Section foodClass) {
			this.item.set(foodClass.name());
		}
		public FoodItem(Section foodClass, String name) {
			this.item.set(name);
			stock = new SimpleObjectProperty<String>(String.valueOf(new Random().nextInt(4)));
			checked.addListener(c->{
				if(checked.get()) {
					decreaseStock();
					checked.set(false);
				}
			});
		}
		//properties, getters & setters 
		private final SimpleBooleanProperty checked = new SimpleBooleanProperty();
		public SimpleBooleanProperty checkedProperty() {return checked;}
		public void setChecked(boolean value) {checked.set(value);}
		public boolean isChecked() {return checked.get();}

		private final SimpleStringProperty item = new SimpleStringProperty();
		public StringProperty itemProperty() {return item;}
		public String getItem() {return item.get();}
		public void setItem(String value) {item.set(value);}

		private SimpleObjectProperty<String> stock;
		public SimpleObjectProperty<String> stockProperty(){ return stock;}
		public String getStock() {return stock.get();}
		public void setStock(String s)	{ Platform.runLater(() -> stock.set(s)); }
		
		//other
		public void decreaseStock() {
			int stock = Integer.parseInt(getStock());
			if(stock>0) {
				stock--;
				setStock(String.valueOf(stock));
				System.out.println("You took a "+ item.get().toLowerCase() + ".");
			}
			else {
				System.out.println("Out of stock");
			}
		}
	}	
}
