package transitionConsumer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class PreviewExample extends Application  {
	private int num = 5;
	private BorderPane bp= new BorderPane();
	private FadeTransitionConsumer fc = null;
	private Insets ins= new Insets(0,10,0,0);
	
	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) {

		try{

			bp.setPrefHeight(150);
			
			final HBox hBox = new HBox();
			BorderPane.setAlignment(hBox, Pos.CENTER);
			BorderPane.setMargin(hBox, new Insets(5,0,5,0));
			
			StackPane sp= new StackPane();
			BorderPane.setAlignment(sp, Pos.CENTER);
			
			ToggleButton [] b = new ToggleButton[num];
			Label [] l = new Label[num];
			fc = new FadeTransitionConsumer();
			for (int i=0;i<num;i++){
				final int j=i;
				
				l[i]= new Label(""+i);
				l[i].setPrefHeight(65);
				l[i].setMaxWidth(500);
				l[i].setAlignment(Pos.CENTER);
				l[i].setStyle("-fx-font-size: 24px");
				l[i].setOpacity(0);
				l[i].setOnMouseClicked((e)->System.out.println(j)); //test for reactivity of visible node
				
				b[i]= new ToggleButton("" + i);
				b[i].setPrefHeight(25);
				b[i].setPrefWidth(85);
			
				fc.add(b[i],l[i], true);
				HBox.setMargin(b[i], ins);
			}
			
			fc.setSelected(b[0], l[0]);
			fc.setDuration(200);
			HBox.setMargin(b[0], new Insets(0,10,0,5));
			HBox.setMargin(b[num-1], new Insets(0,5,0,0));
			fc.changePreview(b[3], false);

			
			//HBox with buttons
			hBox.getChildren().addAll(b);
			bp.setTop(hBox);
			
			//StackPane with labels
			sp.setMaxWidth(500);
			sp.getChildren().addAll(l);
			bp.setCenter(sp);
			
			// show GUI
			Scene scene = new Scene(bp);
			stage.setTitle("FadeTransition Test");
			stage.setOnCloseRequest(e -> {
				fc.stop();
				System.exit(0);
			});
			stage.setScene(scene);
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
