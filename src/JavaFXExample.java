import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXExample extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(new MainMenuPane(primaryStage), 600, 400);
        primaryStage.setTitle("BS Project Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
