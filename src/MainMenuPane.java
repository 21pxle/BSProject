import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Optional;

public class MainMenuPane extends BorderPane {
    private Stage stage;

    public MainMenuPane(Stage stage) {
        this.stage = stage;
        Text text = new Text(180, 200, "Welcome to the main menu!");
        Font font = text.getFont();
        text.setFont(new Font(font.getName(), 20));
        text.setTextAlignment(TextAlignment.CENTER);
        getChildren().add(text);
        HBox box = new HBox(15);
        Button button = new Button("Play");

        button.setOnAction(event -> {
            UserSubstituteMenu menu = new UserSubstituteMenu();
            Optional<ButtonType> type = menu.showAndWait();
            if (type.isPresent() && type.get() == UserSubstituteMenu.PLAY) {
                PlayerList playerList = menu.getPlayers();
                int enemies = playerList.getEnemyPlayers().size();
                int users = playerList.getUserPlayers().size();

                if (users == 0 || users > 5 || enemies == 0 || enemies > 5) {
                    new Alert(Alert.AlertType.WARNING, "You must have at least 1 player on either side,\n" +
                            "but you can have no more than 5 players on either side.", ButtonType.OK).showAndWait();
                    return;
                }

                GameTask gameTask = new GameTask(playerList);
                try {
                    gameTask.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button button1 = new Button("How to Play?");
        button1.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.NONE, "How are you doing?",
                    new ButtonType("Bad"), new ButtonType("OK"), new ButtonType("Good"));
            alert.setTitle("Hi!");
            alert.setHeaderText("Hello there.");
            alert.showAndWait();
        });
        button.setMinSize(100, 50);
        button1.setMinSize(100, 50);
        box.getChildren().addAll(button, button1);
        setBottom(box);
    }

    public Stage getStage() {
        return stage;
    }
}