package userInterface;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ScreenUtils {

    public static ColumnConstraints makeColFromPercent(int value) {
        ColumnConstraints res = new ColumnConstraints();
        res.setPercentWidth(value);
        return res;
    }

    public static RowConstraints makeRowFromPercent(int value) {
        RowConstraints res = new RowConstraints();
        res.setPercentHeight(value);
        return res;
    }

    /**
     * Opens a new error window with a custom error message.
     *
     * @param primaryStage a stage to bind the error window to
     * @param errorMessage a custom error message to display
     */
    public static void generateErrorScreen(Stage primaryStage, String errorMessage) {
        // TODO: style this properly
        Stage stage = new Stage();
        stage.setTitle("Ошибка");


        GridPane root = new GridPane();

        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("default-background");
        root.getRowConstraints().addAll(
                makeRowFromPercent(50),
                makeRowFromPercent(50)
        );

        root.getColumnConstraints().addAll(
                makeColFromPercent(20),
                makeColFromPercent(60),
                makeColFromPercent(20)
        );


        Text text = new Text(errorMessage);
        GridPane.setHalignment(text, HPos.CENTER);
        GridPane.setValignment(text, VPos.CENTER);


        Button ok = new Button("OK");
        GridPane.setValignment(ok, VPos.CENTER);
        GridPane.setHalignment(ok, HPos.CENTER);
        ok.getStyleClass().add("button-font");

        root.add(text, 1, 0);
        root.add(ok, 1, 1);

        ok.setOnAction(event -> {
            stage.getScene().getWindow().hide();
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ok.fire();
                event.consume();
            }
        });

        stage.setScene(new Scene(root, 320, 70));
        stage.setResizable(false);
//        stage.setOnCloseRequest(Event::consume);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }

}
