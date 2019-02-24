package userInterface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

public class Main extends Application {

    final private int APP_WIDTH = 1280;
    final private int APP_HEIGHT = 720;
    final private int MIN_APP_WIDTH = 700;
    final private int MIN_APP_HEIGHT = 300;

    private Scene genScene(Stage primaryStage) {
        GridPane overall = new GridPane();
        {
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setHgrow(Priority.ALWAYS);
//            col1.setFillWidth(true);  // looks like we can omit this
            ColumnConstraints col2 = new ColumnConstraints();
            overall.getColumnConstraints().addAll(col1, col2);
        }
        overall.setGridLinesVisible(true);
        BorderPane leftSide = new BorderPane();
        {
            HBox box = new HBox(5);
            Button b1 = new Button("Продолжить цепочку");
            Button b2 = new Button("Новая цепочка");
            Button b3 = new Button("Добавить нулевую анафору");
            Button b4 = new Button("Отменить");
            box.getChildren().addAll(b1, b2, b3, b4);
            leftSide.setTop(box);
            ScrollPane textWrapper = new ScrollPane();
            textWrapper.setFitToWidth(true);
            FlowPane text = new FlowPane();
            textWrapper.setContent(text);
            text.setPadding(new Insets(5));
//            for (int i = 0; i < 100; i++) text.getChildren().add(new Button("Text"));
            leftSide.setCenter(textWrapper);
            GridPane bottom = new GridPane();
            bottom.setGridLinesVisible(true);
            {
                Button left = new Button("<");
                TextField textField = new TextField();
                Button right = new Button(">");
                ColumnConstraints col1 = new ColumnConstraints();
                ColumnConstraints col2 = new ColumnConstraints();
                col2.setHgrow(Priority.ALWAYS);
//                col2.setFillWidth(true);  // looks like we can omit this
                ColumnConstraints col3 = new ColumnConstraints();
                bottom.getColumnConstraints().addAll(col1, col2, col3);
                bottom.add(left, 0, 0);
                bottom.add(textField, 1, 0);
                bottom.add(right, 2, 0);
            }
            leftSide.setBottom(bottom);
        }
        leftSide.prefHeightProperty().bind(primaryStage.heightProperty());
        GridPane rightSide = new GridPane();
        {
            TextField field = new TextField();
            ScrollPane pane = new ScrollPane();
            RowConstraints row1 = new RowConstraints();
            RowConstraints row2 = new RowConstraints();
            row2.setVgrow(Priority.ALWAYS);
//            row2.setFillHeight(true);  // looks like we can omit this
            rightSide.getRowConstraints().addAll(row1, row2);
            rightSide.add(field, 0, 0);
            rightSide.add(pane, 0, 1);
            rightSide.setGridLinesVisible(true);
        }
        overall.add(leftSide, 0, 0);
        overall.add(rightSide, 1, 0);
        Scene sc = new Scene(overall, APP_WIDTH, APP_HEIGHT);
        sc.getStylesheets().add("styles.css");
        return sc;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Разметка кореференсов");
        Scene sc = genScene(primaryStage);
        primaryStage.setMinWidth(MIN_APP_WIDTH);
        primaryStage.setMinHeight(MIN_APP_HEIGHT);
        primaryStage.setScene(sc);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
