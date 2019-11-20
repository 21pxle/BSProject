import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class AddPlayerMenu extends Alert {

    public static final ButtonType SUBMIT = new ButtonType("Submit");
    private Player player;
    private FileInputStream stream;
    public static void main(String[] args) {
        new Thread(() -> Platform.runLater(() -> {
            new JFXPanel();
            AddPlayerMenu menu = new AddPlayerMenu();
            menu.showAndWait();
        }));
    }

    public AddPlayerMenu() {
        super(AlertType.NONE, "", SUBMIT, ButtonType.CANCEL);
        Pane root = new Pane();
        VBox box = new VBox(10);
        LabelTextField fieldName = new LabelTextField("Name:"), fieldLevel = new LabelTextField("Level:");
        ComboBox<String> classBox = new ComboBox<>();
        classBox.getItems().addAll("Select Class", "Archer", "Mage", "Support", "Warrior", "Tank");
        classBox.getSelectionModel().selectFirst();
        CheckBox checkBox = new CheckBox();
        checkBox.setText("Check if the player is a user.");

        Button button = new Button("Check Status");
        Button button2 = new Button("Use Image From File...");

        button2.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.bmp", "*.gif");
            FileChooser.ExtensionFilter filter1 = new FileChooser.ExtensionFilter("JPG Images", "*.jpg");
            FileChooser.ExtensionFilter filter2 = new FileChooser.ExtensionFilter("PNG Images", "*.png");
            FileChooser.ExtensionFilter filter3 = new FileChooser.ExtensionFilter("BMP Images", "*.bmp");
            FileChooser.ExtensionFilter filter4 = new FileChooser.ExtensionFilter("GIFs", "*.gif");
            fileChooser.getExtensionFilters().addAll(filter, filter1, filter2, filter3, filter4);
            fileChooser.setSelectedExtensionFilter(filter);
            File file = fileChooser.showOpenDialog(null);
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                new Alert(Alert.AlertType.WARNING, "The file cannot be empty.", ButtonType.OK)
                        .showAndWait();
            } //There does not need to be an image.
        });
        box.getChildren().addAll(fieldName, fieldLevel, classBox, checkBox, new HBox(10, button, button2));
        root.getChildren().add(box);
        button.setOnAction(e -> {
            //If EVERY item is not null:
            try {
                if (fieldName.getText() != null && fieldLevel.getText() != null
                        && classBox.getSelectionModel().getSelectedIndex() != 0) {
                    player = new Player(Integer.parseInt(fieldLevel.getText()));
                    player.setName(fieldName.getText());
                    if (checkBox.isSelected()) player.setType(Player.Type.USER);
                    else player.setType(Player.Type.ENEMY);
                    player.setClassType(Player.ClassType.valueOf(classBox.getSelectionModel().getSelectedItem().toUpperCase()));
                    if (stream != null) {
                        player.setImage(stream);
                    }
                    new Alert(Alert.AlertType.NONE, "The player is ready to use.", ButtonType.OK)
                            .showAndWait();
                } else {
                    new Alert(Alert.AlertType.WARNING, "At least one of the required fields is empty.", ButtonType.OK)
                            .showAndWait();
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING, "Please enter a positive integer for the level of the player.", ButtonType.OK)
                        .showAndWait();
            } catch (IllegalArgumentException ex) {
                new Alert(Alert.AlertType.WARNING, "The specified class is not supported at this moment.", ButtonType.OK)
                        .showAndWait();
            }
        });
        getDialogPane().setContent(root);
    }

    public Player getPlayer() {
        return player;
    }
}
