import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class UserSubstituteMenu extends Alert {
    public static final ButtonType PLAY = new ButtonType("Play");
    private PlayerList playerList = new PlayerList();

    public static void main(String[] args) {
        new JFXPanel();
        new Thread(() -> Platform.runLater(() -> {
            UserSubstituteMenu menu = new UserSubstituteMenu();
            menu.showAndWait();
        })).run();
    }

    public UserSubstituteMenu() {
        super(AlertType.NONE, "", PLAY, ButtonType.CANCEL);
        setTitle("Game Setup");

        //Use check boxes
        Pane root = new Pane();
        TableView view = new TableView<Player>();
        TableColumn column = new TableColumn<Player, String>("Name");
        column.setEditable(false);

        TableColumn column2 = new TableColumn<Player, Integer>("Level");
        column2.setEditable(false);

        TableColumn column3 = new TableColumn<Player, ImageView>("Image");
        column2.setEditable(false);

        TableColumn column4 = new TableColumn<Player, Player.ClassType>("Is User?");
        column2.setEditable(false);

        TableColumn column5 = new TableColumn<Player, String>("Class Name");
        column.setCellValueFactory(new PropertyValueFactory<Player, String>("name"));
        column2.setCellValueFactory(new PropertyValueFactory<Player, Integer>("level"));
        column3.setCellValueFactory(new PropertyValueFactory<Player, ImageView>("imageView"));
        column4.setCellValueFactory(new PropertyValueFactory<Player, Boolean>("user"));
        column5.setCellValueFactory(new PropertyValueFactory<Player, Player.ClassType>("classType"));
        view.getColumns().addAll(column, column2, column3, column4, column5);
        column3.setPrefWidth(100);
        //Can add up to 5 players each.
        Button button1 = new Button("Add Player...");
        button1.setOnAction(e -> {
            Player player = null;
            Optional<ButtonType> type;
            while (player == null) {
                AddPlayerMenu menu = new AddPlayerMenu();
                type = menu.showAndWait();
                if (type.isPresent()) {
                    if (type.get() == AddPlayerMenu.SUBMIT) {
                        player = menu.getPlayer();
                        if (player == null) {
                            new Alert(Alert.AlertType.WARNING, "You cannot press \"Submit\" until you check the status of the player.", ButtonType.OK)
                                    .showAndWait();
                        } else {
                            view.getItems().add(player);
                            playerList.add(player);
                        }
                    } else {
                        break;
                    }
                }
            }
        });
        view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        Button button = new Button("Check");
        button.setOnAction(e -> {
            int userPlayers = playerList.getUserPlayers().size();
            int enemyPlayers = playerList.getEnemyPlayers().size();
            if (userPlayers == 0 || userPlayers > 5 || enemyPlayers == 0 || enemyPlayers > 5) {
                new Alert(Alert.AlertType.WARNING, "You must have at least one player on each side,\n" +
                        " but you cannot have more than 5 players on each side.", ButtonType.OK)
                        .showAndWait();
            } else {
                new Alert(Alert.AlertType.NONE, "You are now ready to enter the game.", ButtonType.OK)
                        .showAndWait();
            }
        });
        Button button2 = new Button("Delete Selected Player(s)"),
                button3 = new Button("Move Up"),
                button4 = new Button("Move Down");

        button2.setOnAction(e -> {
            Player selected = (Player) view.getSelectionModel().getSelectedItem();
            view.getItems().remove(selected);
            playerList.remove(selected);
        });

        button3.setOnAction(e -> {
            Player selectedCell = (Player) view.getSelectionModel().getSelectedItem();
            int index = view.getSelectionModel().getSelectedIndex();
            if (index != 0) {
                view.getItems().remove(selectedCell);
                view.getItems().add(index - 1, selectedCell);
            }
        });

        button4.setOnAction(e -> {
            Player selectedCell = (Player) view.getSelectionModel().getSelectedItem();
            int index = view.getSelectionModel().getSelectedIndex();
            if (index != playerList.size() - 1) {
                view.getItems().remove(selectedCell);
                view.getItems().add(index + 1, selectedCell);
            }
        });

        VBox box = new VBox(10, view, new HBox(10, button, button1, button2), new HBox(button3, button4));
        root.getChildren().add(box);

        getDialogPane().setContent(root);
    }

    public PlayerList getPlayers() {
        return playerList;
    }
}
